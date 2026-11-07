package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;
import vn.celineclosset.service.BankTransferService;
import vn.celineclosset.util.AppConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrderDAO extends CrudDAO {
    private static final Object COD_PAYMENT_LOCK = new Object();
    private static volatile boolean completedCodPaymentsNormalized;

    public int createOrder(int maTK, String name, String phone, String address, String note, String payment) throws SQLException {
        return createOrder(maTK, name, phone, address, "", "", null, null, note, payment, null, null);
    }

    public int createOrder(int maTK, String name, String phone, String address, String note, String payment,
                           String[] selectedItemIds, String voucherCode) throws SQLException {
        return createOrder(maTK, name, phone, address, "", "", null, null,
                note, payment, selectedItemIds, voucherCode);
    }

    public int createOrder(int maTK, String name, String phone, String address,
                           String addressArea, String addressDetail, Double deliveryLat, Double deliveryLng,
                           String note, String payment, String[] selectedItemIds, String voucherCode)
            throws SQLException {
        return inTransaction(entityManager -> {
            int maGH = getOrCreateCart(entityManager, maTK);
            List<Integer> selectedIds = parseIds(selectedItemIds);
            List<Map<String, Object>> items = cartItems(entityManager, maGH, selectedIds);
            if (items.isEmpty()) {
                throw new SQLException("Bạn chưa chọn sản phẩm để đặt hàng");
            }

            BigDecimal originalTotal = cartTotal(items);
            Map<String, Object> voucher = findValidVoucher(entityManager, maTK, voucherCode, originalTotal);
            BigDecimal discount = calculateVoucherDiscount(voucher, originalTotal);
            BigDecimal total = originalTotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            String finalNote = buildOrderNote(note, voucherCode, discount, originalTotal);
            Integer processingStaffId = findLeastBusyProcessingStaff(entityManager);
            Integer voucherId = voucher == null ? null : ((Number) voucher.get("maVoucher")).intValue();

            int maDH = insertOrder(entityManager, maTK, processingStaffId, voucherId, name, phone, address,
                    deliveryLat, deliveryLng, finalNote, payment, total, discount);
            if (processingStaffId != null) {
                addStaffLog(entityManager, processingStaffId, maDH, "Tự động phân công xử lý",
                        "Hệ thống tự động phân công xử lý đơn #" + maDH + ".");
                notifyAccount(entityManager, processingStaffId, "Có đơn hàng mới #" + maDH,
                        "Bạn được phân công xử lý đơn mới của " + name + ".",
                        "/admin/order-detail?id=" + maDH, "NEW_ORDER");
            }
            insertOrderItems(entityManager, maDH, items);
            insertPayment(entityManager, maDH, payment, total);
            useVoucher(entityManager, maTK, voucher, maDH);
            clearCartItems(entityManager, maGH, selectedIds, items);
            return maDH;
        });
    }

    private int getOrCreateCart(EntityManager entityManager, int maTK) throws SQLException {
        Map<String, Object> cart = queryOne(entityManager,
                "SELECT TOP 1 maGH FROM GIO_HANG WHERE maTK=? AND trangThai=1 ORDER BY maGH DESC", maTK);
        if (cart != null) return ((Number) cart.get("maGH")).intValue();
        Map<String, Object> created = queryOne(entityManager, """
                INSERT INTO GIO_HANG(maTK,trangThai) OUTPUT INSERTED.maGH AS maGH VALUES(?,1)
                """, maTK);
        if (created == null || created.get("maGH") == null) throw new SQLException("Không tạo được giỏ hàng");
        return ((Number) created.get("maGH")).intValue();
    }

    private List<Map<String, Object>> cartItems(EntityManager entityManager, int maGH, List<Integer> selectedIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT ct.*, sp.tenSP, COALESCE(img.duongDan,sp.hinhAnh) AS hinhAnh FROM CHI_TIET_GIO_HANG ct
                JOIN SAN_PHAM sp ON ct.maSP=sp.maSP
                OUTER APPLY (SELECT TOP 1 ha.duongDan FROM HINH_ANH_SAN_PHAM ha WHERE ha.maSP=ct.maSP AND (ct.mauSac IS NULL OR LTRIM(RTRIM(ha.mauSac))=LTRIM(RTRIM(ct.mauSac))) ORDER BY ha.thuTu,ha.maAnh) img
                WHERE ct.maGH=?
                """);
        List<Object> params = new ArrayList<>();
        params.add(maGH);
        if (selectedIds != null && !selectedIds.isEmpty()) {
            sql.append(" AND ct.maCTGH IN (").append(placeholders(selectedIds.size())).append(") ");
            params.addAll(selectedIds);
        }
        sql.append(" ORDER BY ct.maCTGH DESC");
        return query(entityManager, sql.toString(), params.toArray());
    }

    private BigDecimal cartTotal(List<Map<String, Object>> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) total = total.add((BigDecimal) item.get("thanhTien"));
        return total;
    }

    public BigDecimal voucherDiscount(String voucherCode, BigDecimal total) throws SQLException {
        return voucherDiscount(voucherCode, total, 0);
    }

    public BigDecimal voucherDiscount(String voucherCode, BigDecimal total, int maTK) throws SQLException {
        if (voucherCode == null || voucherCode.isBlank()) return BigDecimal.ZERO;
        Map<String, Object> voucher = findValidVoucher(null, maTK, voucherCode, total);
        return calculateVoucherDiscount(voucher, total);
    }

    public boolean validVoucher(String voucherCode) throws SQLException {
        return validVoucher(voucherCode, BigDecimal.ZERO, 0);
    }

    public boolean validVoucher(String voucherCode, BigDecimal total, int maTK) throws SQLException {
        if (voucherCode == null || voucherCode.isBlank()) return true;
        return findValidVoucher(null, maTK, voucherCode, total) != null;
    }

    private Map<String, Object> findValidVoucher(EntityManager entityManager, int maTK, String voucherCode, BigDecimal total) throws SQLException {
        if (voucherCode == null || voucherCode.isBlank()) return null;
        String sql = """
                SELECT TOP 1 v.*,
                       CASE WHEN v.diemDoi=0 THEN 1
                            WHEN EXISTS(SELECT 1 FROM KHACH_HANG_VOUCHER khv
                                        WHERE khv.maTK=? AND khv.maVoucher=v.maVoucher AND khv.trangThai='AVAILABLE') THEN 1
                            ELSE 0 END AS duocSuDung
                FROM VOUCHER v
                WHERE UPPER(v.maCode)=UPPER(?) AND v.trangThai=1
                  AND v.ngayBatDau<=SYSDATETIME()
                  AND (v.ngayKetThuc IS NULL OR v.ngayKetThuc>=SYSDATETIME())
                  AND (v.soLuot IS NULL OR v.daDung<v.soLuot)
                  AND v.donToiThieu<=?
                """;
        BigDecimal safeTotal = total == null ? BigDecimal.ZERO : total;
        Map<String, Object> row = entityManager == null
                ? queryOne(sql, maTK, voucherCode.trim(), safeTotal)
                : queryOne(entityManager, sql, maTK, voucherCode.trim(), safeTotal);
        return row != null && ((Number) row.get("duocSuDung")).intValue() == 1 ? row : null;
    }

    private BigDecimal calculateVoucherDiscount(Map<String, Object> voucher, BigDecimal total) {
        if (voucher == null || total == null || total.signum() <= 0) return BigDecimal.ZERO;
        BigDecimal value = (BigDecimal) voucher.get("giaTri");
        BigDecimal discount;
        if ("PERCENT".equals(String.valueOf(voucher.get("loaiGiam")))) {
            discount = total.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            Object maxValue = voucher.get("giamToiDa");
            if (maxValue instanceof BigDecimal max && discount.compareTo(max) > 0) discount = max;
        } else {
            discount = value;
        }
        if (discount.compareTo(total) > 0) discount = total;
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildOrderNote(String note, String voucherCode, BigDecimal discount, BigDecimal originalTotal) {
        StringBuilder builder = new StringBuilder();
        if (note != null && !note.isBlank()) builder.append(note.trim());
        if (voucherCode != null && !voucherCode.isBlank() && discount.signum() > 0) {
            if (builder.length() > 0) builder.append(" | ");
            builder.append("Voucher: ").append(voucherCode.trim().toUpperCase())
                    .append(" - Giảm: ").append(discount.stripTrailingZeros().toPlainString())
                    .append(" - Tạm tính: ").append(originalTotal.stripTrailingZeros().toPlainString());
        }
        return builder.toString();
    }

    private Integer findLeastBusyProcessingStaff(EntityManager entityManager) {
        Map<String, Object> row = queryOne(entityManager, """
                SELECT TOP 1 nv.maTK, COUNT(dh.maDH) AS soDonDangXuLy
                FROM TAI_KHOAN nv
                LEFT JOIN DON_HANG dh ON dh.maNhanVienXuLy=nv.maTK
                    AND dh.trangThai NOT IN (N'Hoàn thành',N'Đã hủy',N'Giao thất bại')
                WHERE nv.vaiTro='STAFF' AND nv.trangThai=1
                GROUP BY nv.maTK ORDER BY COUNT(dh.maDH) ASC, nv.maTK ASC
                """);
        return row == null ? null : ((Number) row.get("maTK")).intValue();
    }

    /**
     * Chọn ngẫu nhiên trong nhóm shipper đang có ít đơn hoạt động nhất.
     * Nhờ vậy khi số đơn ít hoặc nhiều shipper có cùng tải, hệ thống không dồn mã đơn nhỏ cho một người cố định.
     */
    private Integer findLeastBusyDelivery(EntityManager entityManager) {
        Map<String, Object> row = queryOne(entityManager, """
                WITH TaiShipper AS (
                    SELECT nv.maTK, COUNT(dh.maDH) AS soDonDangGiao
                    FROM TAI_KHOAN nv
                    LEFT JOIN DON_HANG dh ON dh.maNhanVien=nv.maTK
                        AND dh.trangThai IN (N'Đã xác nhận',N'Đang chuẩn bị',N'Đang giao',N'Đã giao')
                    WHERE nv.vaiTro='DELIVERY' AND nv.trangThai=1
                    GROUP BY nv.maTK
                )
                SELECT TOP 1 maTK,soDonDangGiao
                FROM TaiShipper
                WHERE soDonDangGiao=(SELECT MIN(soDonDangGiao) FROM TaiShipper)
                ORDER BY NEWID()
                """);
        return row == null ? null : ((Number) row.get("maTK")).intValue();
    }

    private int insertOrder(EntityManager entityManager, int maTK, Integer processingStaffId, Integer voucherId,
                            String name, String phone, String address, Double deliveryLat, Double deliveryLng,
                            String note, String payment, BigDecimal total, BigDecimal discount) throws SQLException {
        int points = total.divide(new BigDecimal("10000"), 0, RoundingMode.DOWN).intValue();
        Map<String, Object> created = queryOne(entityManager, """
                INSERT INTO DON_HANG(tongTien,tienGiam,trangThai,phuongThucThanhToan,maTK,maNhanVien,maNhanVienXuLy,maVoucher,
                                     hoTenNhan,soDienThoaiNhan,diaChiNhan,ghiChu,diemCong,viDoGiao,kinhDoGiao)
                OUTPUT INSERTED.maDH AS maDH
                VALUES(?,?,N'Chờ xác nhận',?,?,NULL,?,?,?,?,?,?,?,?,?)
                """, total, discount, payment, maTK, processingStaffId, voucherId, name, phone, address,
                note, points, deliveryLat, deliveryLng);
        if (created == null || created.get("maDH") == null) throw new SQLException("Không tạo được đơn hàng");
        return ((Number) created.get("maDH")).intValue();
    }

    private void insertOrderItems(EntityManager entityManager, int maDH, List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            executeUpdate(entityManager, """
                    INSERT INTO CHI_TIET_DON_HANG(maDH,maSP,soLuong,donGia,thanhTien,mauSac,kichThuoc) VALUES(?,?,?,?,?,?,?)
                    """, maDH, item.get("maSP"), item.get("soLuong"), item.get("donGia"), item.get("thanhTien"), item.get("mauSac"), item.get("kichThuoc"));
            int quantity = ((Number) item.get("soLuong")).intValue();
            int updated = executeUpdate(entityManager, """
                    UPDATE SAN_PHAM SET soLuongTon=soLuongTon-? WHERE maSP=? AND soLuongTon>=?
                    """, quantity, item.get("maSP"), quantity);
            if (updated == 0) throw new RuntimeException("Sản phẩm " + item.get("tenSP") + " không đủ tồn kho.");
        }
    }

    private void insertPayment(EntityManager entityManager, int maDH, String payment, BigDecimal total) {
        String paymentCode = new BankTransferService().paymentCode(maDH);
        executeUpdate(entityManager, """
                INSERT INTO THANH_TOAN(maDH,phuongThuc,soTien,noiDungChuyenKhoan,trangThai)
                VALUES(?,?,?,?, 'PENDING')
                """, maDH, payment, total, paymentCode);
    }

    private void useVoucher(EntityManager entityManager, int maTK, Map<String, Object> voucher, int maDH) {
        if (voucher == null) return;
        int voucherId = ((Number) voucher.get("maVoucher")).intValue();
        executeUpdate(entityManager, "UPDATE VOUCHER SET daDung=daDung+1 WHERE maVoucher=?", voucherId);
        if (((Number) voucher.get("diemDoi")).intValue() > 0) {
            executeUpdate(entityManager, """
                    UPDATE TOP (1) KHACH_HANG_VOUCHER
                    SET trangThai='USED', ngaySuDung=SYSDATETIME(), maDH=?
                    WHERE maTK=? AND maVoucher=? AND trangThai='AVAILABLE'
                    """, maDH, maTK, voucherId);
        }
    }

    private void clearCartItems(EntityManager entityManager, int maGH, List<Integer> selectedIds,
                                List<Map<String, Object>> orderedItems) {
        List<Integer> ids = selectedIds == null || selectedIds.isEmpty() ? new ArrayList<>() : new ArrayList<>(selectedIds);
        if (ids.isEmpty()) for (Map<String, Object> item : orderedItems) ids.add(((Number) item.get("maCTGH")).intValue());
        if (ids.isEmpty()) return;
        List<Object> params = new ArrayList<>();
        params.add(maGH);
        params.addAll(ids);
        executeUpdate(entityManager, "DELETE FROM CHI_TIET_GIO_HANG WHERE maGH=? AND maCTGH IN ("
                + placeholders(ids.size()) + ")", params.toArray());
    }

    /**
     * Đồng bộ dữ liệu cũ: đơn COD đã Hoàn thành đồng nghĩa shipper đã thu tiền.
     * Chạy một lần mỗi vòng đời ứng dụng để các đơn seed/cũ không còn PENDING sai.
     */
    private void normalizeCompletedCodPayments() throws SQLException {
        if (completedCodPaymentsNormalized) return;
        synchronized (COD_PAYMENT_LOCK) {
            if (completedCodPaymentsNormalized) return;
            executeUpdate("""
                    UPDATE tt
                    SET tt.trangThai='PAID',tt.soTienDaNhan=tt.soTien,tt.trangThaiDoiSoat='NONE',
                        tt.ghiChuDoiSoat=CASE WHEN ISNULL(tt.ghiChuDoiSoat,N'')='' THEN N'Tự động đồng bộ COD đã giao thành công' ELSE tt.ghiChuDoiSoat END,
                        tt.ngayThanhToan=COALESCE(tt.ngayThanhToan,dh.ngayHoanThanh,SYSDATETIME()),
                        tt.ngayCapNhat=SYSDATETIME()
                    FROM THANH_TOAN tt
                    JOIN DON_HANG dh ON dh.maDH=tt.maDH
                    WHERE dh.trangThai=N'Hoàn thành' AND dh.phuongThucThanhToan='COD'
                      AND COALESCE(tt.trangThai,'PENDING')<>'PAID'
                    """);
            completedCodPaymentsNormalized = true;
        }
    }

    /**
     * Lịch sử mua hàng của khách không hiển thị đơn BANK đã hết hạn thanh toán do hệ thống tự xử lý.
     * Bản ghi vẫn được giữ trong database để ADMIN/STAFF đối soát nếu tiền về muộn.
     */
    public List<Map<String, Object>> myOrders(int maTK) throws SQLException {
        normalizeCompletedCodPayments();
        return query(orderSelect() + " WHERE dh.maTK=? " + hiddenExpiredPaymentFilter() + " ORDER BY dh.maDH DESC", maTK);
    }

    public Map<String, Object> myOrderById(int maTK, int maDH) throws SQLException {
        normalizeCompletedCodPayments();
        return queryOne(orderSelect() + " WHERE dh.maTK=? AND dh.maDH=? " + hiddenExpiredPaymentFilter(), maTK, maDH);
    }

    public List<Map<String, Object>> customerOrders(int maTK) throws SQLException {
        normalizeCompletedCodPayments();
        return query("""
                SELECT dh.*
                FROM DON_HANG dh
                LEFT JOIN THANH_TOAN tt ON tt.maDH=dh.maDH
                WHERE dh.maTK=?
                  AND NOT (COALESCE(tt.phuongThuc,'')='BANK' AND COALESCE(tt.trangThai,'')='FAILED' AND COALESCE(dh.nguoiHuy,'')='SYSTEM')
                ORDER BY dh.maDH DESC
                """, maTK);
    }

    private String hiddenExpiredPaymentFilter() {
        return " AND NOT (COALESCE(tt.phuongThuc,'')='BANK' AND COALESCE(tt.trangThai,'')='FAILED' AND COALESCE(dh.nguoiHuy,'')='SYSTEM') ";
    }

    public List<Map<String, Object>> allOrders() throws SQLException {
        return adminOrders(null, null, null, "ADMIN", 0);
    }

    public List<Map<String, Object>> adminOrders(String q, String orderStatus, String paymentStatus) throws SQLException {
        return adminOrders(q, orderStatus, paymentStatus, "ADMIN", 0);
    }

    public List<Map<String, Object>> adminOrders(String q, String orderStatus, String paymentStatus,
                                                  String role, int accountId) throws SQLException {
        normalizeCompletedCodPayments();
        StringBuilder sql = new StringBuilder(orderSelect()).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if ("DELIVERY".equals(role)) {
            sql.append("AND dh.maNhanVien=? AND dh.trangThai IN (N'Đã xác nhận',N'Đang chuẩn bị',N'Đang giao',N'Đã giao',N'Hoàn thành',N'Giao thất bại') ");
            params.add(accountId);
        } else if ("STAFF".equals(role)) {
            sql.append("AND dh.maNhanVienXuLy=? ");
            params.add(accountId);
        }
        appendOrderFilters(sql, params, q, orderStatus, paymentStatus);
        sql.append("ORDER BY dh.maDH DESC");
        return query(sql.toString(), params.toArray());
    }

    public Map<String, Object> orderById(int maDH) throws SQLException {
        normalizeCompletedCodPayments();
        return queryOne(orderSelect() + " WHERE dh.maDH=?", maDH);
    }

    public Map<String, Object> orderByIdForUser(int maDH, String role, int accountId) throws SQLException {
        if ("DELIVERY".equals(role)) return queryOne(orderSelect() + " WHERE dh.maDH=? AND dh.maNhanVien=?", maDH, accountId);
        if ("STAFF".equals(role)) return queryOne(orderSelect() + " WHERE dh.maDH=? AND dh.maNhanVienXuLy=?", maDH, accountId);
        return orderById(maDH);
    }

    public List<Map<String, Object>> ordersForTracking(String q, String status) throws SQLException {
        return ordersForTracking(q, status, "ADMIN", 0);
    }

    public List<Map<String, Object>> ordersForTracking(String q, String status, String role, int accountId) throws SQLException {
        normalizeCompletedCodPayments();
        StringBuilder sql = new StringBuilder(orderSelect()).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if ("DELIVERY".equals(role)) {
            sql.append("AND dh.maNhanVien=? AND dh.trangThai IN (N'Đã xác nhận',N'Đang chuẩn bị',N'Đang giao',N'Đã giao',N'Hoàn thành',N'Giao thất bại') ");
            params.add(accountId);
        } else if ("STAFF".equals(role)) {
            sql.append("AND dh.maNhanVienXuLy=? ");
            params.add(accountId);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND dh.trangThai=? ");
            params.add(status);
        }
        if (q != null && !q.trim().isEmpty()) {
            String like = "%" + q.trim() + "%";
            sql.append("AND (CAST(dh.maDH AS VARCHAR(20)) LIKE ? OR dh.hoTenNhan LIKE ? OR dh.soDienThoaiNhan LIKE ? OR tk.hoTen LIKE ? OR tk.email LIKE ?) ");
            Collections.addAll(params, like, like, like, like, like);
        }
        sql.append("ORDER BY CASE WHEN dh.trangThai=N'Đang giao' THEN 0 ELSE 1 END, dh.maDH DESC");
        return query(sql.toString(), params.toArray());
    }

    private String orderSelect() {
        return """
                SELECT dh.*, tk.hoTen AS tenTaiKhoan, tk.email,
                       nv.hoTen AS tenNhanVien, nv.email AS emailNhanVien,
                       xl.hoTen AS tenNhanVienXuLy, xl.email AS emailNhanVienXuLy,
                       tt.maTT, tt.phuongThuc AS phuongThucTT, tt.noiDungChuyenKhoan,
                       tt.soTien AS soTienThanhToan,tt.soTienDaNhan,tt.trangThaiDoiSoat,tt.ghiChuDoiSoat,
                       tt.maGiaoDichNganHang,tt.maGiaoDichSePay,tt.ngayThanhToan,
                       COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan,
                       yc.maYCTH AS maYeuCauTraHang,yc.trangThai AS trangThaiTraHang,
                       CASE WHEN yc.maYCTH IS NULL THEN 0 ELSE 1 END AS hasReturnRequest,
                       v.maCode AS voucherCode, v.tenVoucher
                FROM DON_HANG dh
                JOIN TAI_KHOAN tk ON dh.maTK=tk.maTK
                LEFT JOIN TAI_KHOAN nv ON nv.maTK=dh.maNhanVien
                LEFT JOIN TAI_KHOAN xl ON xl.maTK=dh.maNhanVienXuLy
                LEFT JOIN THANH_TOAN tt ON tt.maDH=dh.maDH
                LEFT JOIN YEU_CAU_TRA_HANG yc ON yc.maDH=dh.maDH
                LEFT JOIN VOUCHER v ON v.maVoucher=dh.maVoucher
                """;
    }

    private void appendOrderFilters(StringBuilder sql, List<Object> params, String q, String orderStatus, String paymentStatus) {
        if (orderStatus != null && !orderStatus.isBlank()) {
            sql.append("AND dh.trangThai=? ");
            params.add(orderStatus);
        }
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            sql.append("AND COALESCE(tt.trangThai,'PENDING')=? ");
            params.add(paymentStatus);
        }
        if (q != null && !q.trim().isEmpty()) {
            String like = "%" + q.trim() + "%";
            sql.append("AND (CAST(dh.maDH AS VARCHAR(20)) LIKE ? OR dh.hoTenNhan LIKE ? OR dh.soDienThoaiNhan LIKE ? OR tk.hoTen LIKE ? OR tk.email LIKE ? OR nv.hoTen LIKE ? OR xl.hoTen LIKE ?) ");
            Collections.addAll(params, like, like, like, like, like, like, like);
        }
    }

    public List<Map<String, Object>> orderItems(int maDH) throws SQLException {
        return query("""
                SELECT ct.*, sp.tenSP, sp.hinhAnh, sp.maDM, dm.tenDM
                FROM CHI_TIET_DON_HANG ct JOIN SAN_PHAM sp ON ct.maSP=sp.maSP
                LEFT JOIN DANH_MUC dm ON dm.maDM=sp.maDM WHERE ct.maDH=? ORDER BY ct.maCTDH DESC
                """, maDH);
    }

    /** Sản phẩm của đơn khách hàng, kèm cờ đã đánh giá để không hiện nút đánh giá lặp. */
    public List<Map<String, Object>> orderItemsForCustomer(int maDH, int customerId) throws SQLException {
        return query("""
                SELECT ct.*,sp.tenSP,sp.hinhAnh,sp.maDM,dm.tenDM,
                       CASE WHEN EXISTS(SELECT 1 FROM PHAN_HOI ph
                                        WHERE ph.maTK=? AND ph.maSP=ct.maSP AND ph.maDH=ct.maDH AND ph.trangThai=1)
                            THEN 1 ELSE 0 END AS daDanhGia
                FROM CHI_TIET_DON_HANG ct
                JOIN DON_HANG dh ON dh.maDH=ct.maDH AND dh.maTK=?
                JOIN SAN_PHAM sp ON ct.maSP=sp.maSP
                LEFT JOIN DANH_MUC dm ON dm.maDM=sp.maDM
                WHERE ct.maDH=? ORDER BY ct.maCTDH DESC
                """, customerId, customerId, maDH);
    }

    public void assignStaff(int maDH, int staffId) throws SQLException {
        inTransaction(entityManager -> {
            Map<String, Object> staff = queryOne(entityManager,
                    "SELECT maTK FROM TAI_KHOAN WHERE maTK=? AND vaiTro='DELIVERY' AND trangThai=1", staffId);
            if (staff == null) throw new SQLException("Nhân viên không hợp lệ hoặc đang bị khóa.");
            executeUpdate(entityManager, "UPDATE DON_HANG SET maNhanVien=? WHERE maDH=?", staffId, maDH);
            addStaffLog(entityManager, staffId, maDH, "Nhận phân công",
                    "Chủ cửa hàng phân công phụ trách đơn #" + maDH + ".");
            return null;
        });
    }

    public boolean staffOwnsOrder(int maDH, int staffId) throws SQLException {
        return queryOne("SELECT maDH FROM DON_HANG WHERE maDH=? AND maNhanVien=?", maDH, staffId) != null;
    }

    public boolean processingStaffOwnsOrder(int maDH, int staffId) throws SQLException {
        return queryOne("SELECT maDH FROM DON_HANG WHERE maDH=? AND maNhanVienXuLy=?", maDH, staffId) != null;
    }

    public void updateOrderStatus(int maDH, String status) throws SQLException {
        throw new IllegalStateException("Cập nhật trạng thái phải có tài khoản thực hiện.");
    }

    /**
     * STAFF/DELIVERY chỉ đi đúng một bước về phía trước. Khi STAFF chuyển đơn sang
     * “Đang giao”, hệ thống tự chọn ngẫu nhiên trong nhóm DELIVERY ít đơn nhất và
     * gửi thông báo cho người được phân công.
     */
    public void updateOrderStatus(int maDH, String status, Integer actorId) throws SQLException {
        if (actorId == null) throw new IllegalStateException("Không xác định được người cập nhật trạng thái.");
        String requested = status == null ? "" : status.trim();
        inTransaction(entityManager -> {
            Map<String, Object> current = queryOne(entityManager, """
                    SELECT dh.trangThai,dh.maNhanVien,dh.maNhanVienXuLy,dh.phuongThucThanhToan,
                           COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    LEFT JOIN THANH_TOAN tt WITH (UPDLOCK,ROWLOCK) ON tt.maDH=dh.maDH
                    WHERE dh.maDH=?
                    """, maDH);
            if (current == null) throw new IllegalStateException("Không tìm thấy đơn hàng.");

            String currentStatus = String.valueOf(current.get("trangThai"));
            String actorRole = roleOf(entityManager, actorId);
            if (actorRole == null) throw new IllegalStateException("Tài khoản không có quyền cập nhật đơn.");

            if ("DELIVERY".equals(actorRole)) {
                Object owner = current.get("maNhanVien");
                if (!(owner instanceof Number number) || number.intValue() != actorId) {
                    throw new IllegalStateException("Đơn hàng không thuộc shipper này.");
                }
            }

            String next = nextAllowedStatus(currentStatus, actorRole);
            if (next == null) {
                throw new IllegalStateException("Đơn đã ở trạng thái cuối hoặc vai trò hiện tại không được cập nhật bước tiếp theo.");
            }
            if (!next.equals(requested)) {
                throw new IllegalStateException("Không thể chuyển từ “" + currentStatus + "” sang “" + requested
                        + "”. Bước hợp lệ tiếp theo là “" + next + "”.");
            }

            boolean confirms = "Đã xác nhận".equals(requested)
                    && ("STAFF".equals(actorRole) || "ADMIN".equals(actorRole));
            Integer assignedDelivery = current.get("maNhanVien") instanceof Number n ? n.intValue() : null;
            boolean newlyAssigned = false;

            if (confirms) {
                executeUpdate(entityManager, """
                        UPDATE DON_HANG
                        SET trangThai=?,maNhanVienXuLy=COALESCE(maNhanVienXuLy,?),
                            ngayXacNhan=COALESCE(ngayXacNhan,SYSDATETIME())
                        WHERE maDH=?
                        """, requested, actorId, maDH);
            } else if ("Đang giao".equals(requested)) {
                if (assignedDelivery == null) {
                    assignedDelivery = findLeastBusyDelivery(entityManager);
                    if (assignedDelivery == null) {
                        throw new IllegalStateException("Chưa có tài khoản DELIVERY đang hoạt động để nhận đơn giao.");
                    }
                    newlyAssigned = true;
                }
                executeUpdate(entityManager,
                        "UPDATE DON_HANG SET trangThai=?,maNhanVien=? WHERE maDH=?",
                        requested, assignedDelivery, maDH);
                if (newlyAssigned) {
                    addStaffLog(entityManager, assignedDelivery, maDH, "Tự động nhận đơn giao",
                            "Hệ thống phân công đơn #" + maDH + " khi đơn chuyển sang Đang giao.");
                    notifyAccount(entityManager, assignedDelivery, "Bạn có đơn giao mới #" + maDH,
                            "Đơn #" + maDH + " vừa được chuyển sang Đang giao và phân công cho bạn.",
                            "/admin/order-tracking?id=" + maDH, "DELIVERY_ASSIGNED");
                }
            } else if (N_COMPLETED.equals(requested)) {
                String paymentMethod = String.valueOf(current.get("phuongThucThanhToan"));
                String paymentStatus = String.valueOf(current.get("trangThaiThanhToan"));
                if ("BANK".equals(paymentMethod) && !"PAID".equals(paymentStatus)) {
                    throw new IllegalStateException("Đơn chuyển khoản chưa được xác nhận thanh toán nên chưa thể hoàn tất giao hàng.");
                }
                executeUpdate(entityManager, """
                        UPDATE DON_HANG
                        SET trangThai=?,ngayHoanThanh=COALESCE(ngayHoanThanh,SYSDATETIME())
                        WHERE maDH=?
                        """, requested, maDH);
                if ("COD".equals(paymentMethod)) {
                    executeUpdate(entityManager, """
                            UPDATE THANH_TOAN
                            SET trangThai='PAID',soTienDaNhan=soTien,trangThaiDoiSoat='NONE',
                                ghiChuDoiSoat=N'Tự động xác nhận tiền mặt khi giao thành công',
                                ngayThanhToan=COALESCE(ngayThanhToan,SYSDATETIME()),ngayCapNhat=SYSDATETIME()
                            WHERE maDH=?
                            """, maDH);
                }
            } else {
                executeUpdate(entityManager, "UPDATE DON_HANG SET trangThai=? WHERE maDH=?", requested, maDH);
            }

            if (isBackOfficeActor(entityManager, actorId)) {
                addStaffLog(entityManager, actorId, maDH,
                        confirms ? "Xác nhận đơn" : "Cập nhật trạng thái",
                        "Đã chuyển đơn #" + maDH + " từ " + currentStatus + " sang " + requested + ".");
            }
            if (N_COMPLETED.equals(requested)) awardPoints(entityManager, maDH);
            return null;
        });
    }

    public String nextAllowedStatus(String currentStatus, String role) {
        if (currentStatus == null || role == null) return null;
        return switch (currentStatus) {
            case "Chờ xác nhận" -> ("ADMIN".equals(role) || "STAFF".equals(role)) ? "Đã xác nhận" : null;
            case "Đã xác nhận" -> ("ADMIN".equals(role) || "STAFF".equals(role)) ? "Đang chuẩn bị" : null;
            case "Đang chuẩn bị" -> ("ADMIN".equals(role) || "STAFF".equals(role) || "DELIVERY".equals(role)) ? "Đang giao" : null;
            // Shipper phải báo đã giao trước. CUSTOMER mới là người xác nhận nhận hàng để hoàn tất đơn.
            case "Đang giao" -> ("ADMIN".equals(role) || "DELIVERY".equals(role)) ? "Đã giao" : null;
            case "Đã giao" -> null;
            default -> null;
        };
    }

    /** Khách hàng chỉ được xác nhận sau khi shipper đã chuyển đơn sang “Đã giao”. */
    public void confirmReceivedByCustomer(int customerId, int maDH) throws SQLException {
        inTransaction(entityManager -> {
            Map<String, Object> current = queryOne(entityManager, """
                    SELECT dh.maTK,dh.trangThai,dh.phuongThucThanhToan,dh.maNhanVien,dh.maNhanVienXuLy,
                           COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    LEFT JOIN THANH_TOAN tt WITH (UPDLOCK,ROWLOCK) ON tt.maDH=dh.maDH
                    WHERE dh.maDH=? AND dh.maTK=?
                    """, maDH, customerId);
            if (current == null) throw new IllegalStateException("Đơn hàng không thuộc tài khoản của bạn.");
            String status = String.valueOf(current.get("trangThai"));
            if (N_COMPLETED.equals(status)) return null;
            if (!"Đã giao".equals(status)) {
                throw new IllegalStateException("Shipper chưa xác nhận đã giao hàng. Bạn chỉ có thể bấm Đã nhận hàng sau khi trạng thái chuyển sang “Đã giao”.");
            }
            String method = String.valueOf(current.get("phuongThucThanhToan"));
            String paymentStatus = String.valueOf(current.get("trangThaiThanhToan"));
            if ("BANK".equals(method) && !"PAID".equals(paymentStatus)) {
                throw new IllegalStateException("Đơn chuyển khoản chưa được xác nhận thanh toán.");
            }

            executeUpdate(entityManager, """
                    UPDATE DON_HANG
                    SET trangThai=N'Hoàn thành',ngayHoanThanh=COALESCE(ngayHoanThanh,SYSDATETIME())
                    WHERE maDH=? AND maTK=?
                    """, maDH, customerId);
            if ("COD".equals(method)) {
                executeUpdate(entityManager, """
                        UPDATE THANH_TOAN
                        SET trangThai='PAID',soTienDaNhan=soTien,trangThaiDoiSoat='NONE',
                            ghiChuDoiSoat=N'Khách hàng xác nhận đã nhận hàng',
                            ngayThanhToan=COALESCE(ngayThanhToan,SYSDATETIME()),ngayCapNhat=SYSDATETIME()
                        WHERE maDH=?
                        """, maDH);
            }
            awardPoints(entityManager, maDH);
            if (current.get("maNhanVien") instanceof Number delivery) {
                notifyAccount(entityManager, delivery.intValue(), "Khách đã nhận đơn #" + maDH,
                        "Khách hàng đã bấm xác nhận Đã nhận hàng cho đơn #" + maDH + ".",
                        "/admin/order-detail?id=" + maDH, "CUSTOMER_RECEIVED");
            }
            if (current.get("maNhanVienXuLy") instanceof Number staff) {
                notifyAccount(entityManager, staff.intValue(), "Đơn #" + maDH + " đã hoàn thành",
                        "Khách hàng đã xác nhận nhận hàng thành công.",
                        "/admin/order-detail?id=" + maDH, "CUSTOMER_RECEIVED");
            }
            return null;
        });
    }

    /**
     * Quyền sửa lùi dành riêng cho ADMIN khi nhân viên thao tác nhầm.
     * Chỉ các trạng thái vận hành chưa hoàn tất mới được sửa lùi và bắt buộc ghi lý do.
     */
    public void adminOverrideOrderStatus(int maDH, String targetStatus, int adminId, String reason) throws SQLException {
        String target = targetStatus == null ? "" : targetStatus.trim();
        String cleanReason = reason == null ? "" : reason.trim();
        if (cleanReason.length() < 5) throw new IllegalStateException("ADMIN phải nhập lý do sửa lùi trạng thái.");
        List<String> flow = List.of("Chờ xác nhận", "Đã xác nhận", "Đang chuẩn bị", "Đang giao", "Đã giao");
        if (!flow.contains(target)) throw new IllegalStateException("Trạng thái cần sửa không hợp lệ.");

        inTransaction(entityManager -> {
            if (!"ADMIN".equals(roleOf(entityManager, adminId))) {
                throw new IllegalStateException("Chỉ ADMIN được sửa lùi trạng thái đơn.");
            }
            Map<String, Object> order = queryOne(entityManager, """
                    SELECT dh.trangThai,dh.maNhanVien,dh.maNhanVienXuLy,yc.maYCTH
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    LEFT JOIN YEU_CAU_TRA_HANG yc ON yc.maDH=dh.maDH
                    WHERE dh.maDH=?
                    """, maDH);
            if (order == null) throw new IllegalStateException("Không tìm thấy đơn hàng.");
            String current = String.valueOf(order.get("trangThai"));
            int currentIndex = flow.indexOf(current);
            int targetIndex = flow.indexOf(target);
            if (currentIndex < 0) {
                throw new IllegalStateException("Không thể sửa lùi đơn đã hoàn tất, đã hủy hoặc đang xử lý sự cố.");
            }
            if (order.get("maYCTH") != null) {
                throw new IllegalStateException("Đơn đã có yêu cầu trả hàng nên không thể sửa trạng thái giao hàng.");
            }
            if (targetIndex >= currentIndex) {
                throw new IllegalStateException("Chức năng ADMIN này chỉ dùng để sửa về một bước trước đó.");
            }

            boolean withdrawDeliveryAssignment = "Đang giao".equals(current) && targetIndex < flow.indexOf("Đang giao");
            executeUpdate(entityManager, """
                    UPDATE DON_HANG SET trangThai=?,
                        ngayXacNhan=CASE WHEN ?=N'Chờ xác nhận' THEN NULL ELSE ngayXacNhan END,
                        maNhanVien=CASE WHEN ?=1 THEN NULL ELSE maNhanVien END
                    WHERE maDH=?
                    """, target, target, withdrawDeliveryAssignment ? 1 : 0, maDH);
            addStaffLog(entityManager, adminId, maDH, "ADMIN sửa lùi trạng thái",
                    "Sửa từ " + current + " về " + target + ". Lý do: " + cleanReason
                            + (withdrawDeliveryAssignment ? " Đã thu hồi phân công giao do đơn chưa thực sự ở bước Đang giao." : ""));

            if (order.get("maNhanVienXuLy") instanceof Number staff) {
                notifyAccount(entityManager, staff.intValue(), "ADMIN đã điều chỉnh đơn #" + maDH,
                        "Trạng thái được sửa từ " + current + " về " + target + ". Lý do: " + cleanReason,
                        "/admin/order-detail?id=" + maDH, "ORDER_STATUS_CORRECTED");
            }
            if (order.get("maNhanVien") instanceof Number delivery) {
                notifyAccount(entityManager, delivery.intValue(), "ADMIN đã điều chỉnh đơn giao #" + maDH,
                        "Trạng thái được sửa từ " + current + " về " + target + ". Lý do: " + cleanReason
                                + (withdrawDeliveryAssignment ? " Phân công giao tạm thời đã được thu hồi." : ""),
                        "/admin/order-tracking?id=" + maDH, "ORDER_STATUS_CORRECTED");
            }
            return null;
        });
    }

    private String roleOf(EntityManager entityManager, int accountId) {
        Map<String,Object> row = queryOne(entityManager,
                "SELECT vaiTro FROM TAI_KHOAN WHERE maTK=? AND trangThai=1", accountId);
        return row == null ? null : String.valueOf(row.get("vaiTro"));
    }

    private static final String N_COMPLETED = "Hoàn thành";

    private void awardPoints(EntityManager entityManager, int maDH) {
        Map<String, Object> order = queryOne(entityManager,
                "SELECT maTK,diemCong,daCongDiem FROM DON_HANG WITH (UPDLOCK,ROWLOCK) WHERE maDH=?", maDH);
        if (order == null || Boolean.TRUE.equals(order.get("daCongDiem"))
                || (order.get("daCongDiem") instanceof Number n && n.intValue() == 1)) return;
        int accountId = ((Number) order.get("maTK")).intValue();
        int points = ((Number) order.get("diemCong")).intValue();
        executeUpdate(entityManager, "UPDATE DON_HANG SET daCongDiem=1 WHERE maDH=?", maDH);
        executeUpdate(entityManager, "UPDATE TAI_KHOAN SET diemTichLuy=diemTichLuy+? WHERE maTK=?", points, accountId);
        executeUpdate(entityManager, """
                INSERT INTO LICH_SU_DIEM(maTK,maDH,soDiem,loai,noiDung)
                VALUES(?,?,?,'EARN',N'Tích điểm từ đơn hàng hoàn thành')
                """, accountId, maDH, points);
        refreshTier(entityManager, accountId);
    }

    private void refreshTier(EntityManager entityManager, int accountId) {
        executeUpdate(entityManager, """
                UPDATE TAI_KHOAN SET hangThanhVien=CASE
                    WHEN diemTichLuy>=7000 THEN 'DIAMOND'
                    WHEN diemTichLuy>=3000 THEN 'PLATINUM'
                    WHEN diemTichLuy>=1000 THEN 'GOLD'
                    WHEN diemTichLuy>=300 THEN 'SILVER'
                    ELSE 'BRONZE' END WHERE maTK=?
                """, accountId);
    }

    public void updatePaymentStatus(int maDH, String paymentStatus, int actorId) throws SQLException {
        String requested = paymentStatus == null ? "" : paymentStatus.trim().toUpperCase();
        if (!List.of("PENDING", "PAID", "FAILED", "CANCELLED").contains(requested)) {
            throw new IllegalStateException("Trạng thái thanh toán không hợp lệ.");
        }
        inTransaction(entityManager -> {
            String role = roleOf(entityManager, actorId);
            if (!("ADMIN".equals(role) || "STAFF".equals(role))) {
                throw new IllegalStateException("Tài khoản không có quyền cập nhật thanh toán.");
            }
            Map<String, Object> order = queryOne(entityManager, """
                    SELECT dh.trangThai,dh.phuongThucThanhToan,dh.tongTien,
                           COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan,
                           yc.maYCTH,yc.trangThai AS trangThaiTraHang
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    LEFT JOIN THANH_TOAN tt WITH (UPDLOCK,ROWLOCK) ON tt.maDH=dh.maDH
                    LEFT JOIN YEU_CAU_TRA_HANG yc ON yc.maDH=dh.maDH
                    WHERE dh.maDH=?
                    """, maDH);
            if (order == null) throw new IllegalStateException("Không tìm thấy đơn hàng.");
            String currentPayment = String.valueOf(order.get("trangThaiThanhToan"));
            String orderStatus = String.valueOf(order.get("trangThai"));
            String method = String.valueOf(order.get("phuongThucThanhToan"));

            if (order.get("maYCTH") != null && !requested.equals(currentPayment)) {
                throw new IllegalStateException("Đơn đang có quy trình trả hàng/hoàn tiền. Không được đổi trạng thái thanh toán để tránh hai luồng đè lên nhau.");
            }
            if ("Hoàn thành".equals(orderStatus) && !"PAID".equals(requested)) {
                throw new IllegalStateException("Đơn đã giao hoàn tất không thể chuyển thanh toán sang lỗi hoặc hủy.");
            }
            String effectiveStatus = ("COD".equals(method) && "Hoàn thành".equals(orderStatus)) ? "PAID" : requested;

            int updated;
            if ("PAID".equals(effectiveStatus)) {
                updated = executeUpdate(entityManager, """
                        UPDATE THANH_TOAN SET trangThai='PAID',soTienDaNhan=soTien,trangThaiDoiSoat='NONE',
                            ghiChuDoiSoat=N'Nhân viên xác nhận thủ công',ngayThanhToan=COALESCE(ngayThanhToan,SYSDATETIME()),
                            ngayCapNhat=SYSDATETIME() WHERE maDH=?
                        """, maDH);
            } else {
                updated = executeUpdate(entityManager, """
                        UPDATE THANH_TOAN SET trangThai=?,soTienDaNhan=CASE WHEN ?='PENDING' THEN soTienDaNhan ELSE 0 END,
                            trangThaiDoiSoat=CASE WHEN ?='PENDING' THEN trangThaiDoiSoat ELSE 'NONE' END,
                            ngayThanhToan=NULL,ngayCapNhat=SYSDATETIME() WHERE maDH=?
                        """, effectiveStatus, effectiveStatus, effectiveStatus, maDH);
            }
            if (updated == 0) {
                String paymentCode = new BankTransferService().paymentCode(maDH);
                executeUpdate(entityManager, """
                        INSERT INTO THANH_TOAN(maDH,phuongThuc,soTien,noiDungChuyenKhoan,ngayThanhToan,trangThai,soTienDaNhan)
                        SELECT maDH,phuongThucThanhToan,tongTien,?,
                               CASE WHEN ?='PAID' THEN SYSDATETIME() ELSE NULL END,?,
                               CASE WHEN ?='PAID' THEN tongTien ELSE 0 END
                        FROM DON_HANG WHERE maDH=?
                        """, paymentCode, effectiveStatus, effectiveStatus, effectiveStatus, maDH);
            }
            addStaffLog(entityManager, actorId, maDH, "Cập nhật thanh toán",
                    "Chuyển thanh toán từ " + currentPayment + " sang " + effectiveStatus + ".");
            return null;
        });
    }

    public void cancelOrderByCustomer(int customerId, int maDH, String reason, String otherReason) throws SQLException {
        Map<String, Object> order = queryOne("SELECT maDH,trangThai FROM DON_HANG WHERE maDH=? AND maTK=?", maDH, customerId);
        if (order == null) throw new IllegalStateException("Không tìm thấy đơn hàng.");
        String status = String.valueOf(order.get("trangThai"));
        if (!("Chờ xác nhận".equals(status) || "Đã xác nhận".equals(status) || "Đang chuẩn bị".equals(status))) {
            throw new IllegalStateException("Đơn đã được nhân viên giao hàng nhận nên không thể hủy.");
        }
        String finalReason = cleanNote(reason);
        if ("Khác".equals(finalReason)) {
            if (otherReason == null || otherReason.isBlank()) throw new IllegalStateException("Vui lòng nhập lý do khác.");
            finalReason = "Khác: " + otherReason.trim();
        }
        cancelOrder(maDH, "Khách hàng: " + finalReason);
    }

    public void cancelOrder(int maDH, String reason) throws SQLException {
        String text = cleanNote(reason);
        inTransaction(entityManager -> {
            restoreStockOnce(entityManager, maDH);
            executeUpdate(entityManager, """
                    UPDATE DON_HANG SET trangThai=N'Đã hủy', lyDoHuy=?, ngayHuy=SYSDATETIME(),
                    nguoiHuy=CASE WHEN ? LIKE N'Khách hàng:%' THEN 'CUSTOMER' ELSE 'STAFF' END,
                    ghiChu=CONCAT(ISNULL(ghiChu,N''),CASE WHEN ISNULL(ghiChu,N'')='' THEN N'' ELSE N' | ' END,N'Lý do hủy: ',?)
                    WHERE maDH=?
                    """, text, text, text, maDH);
            executeUpdate(entityManager, "UPDATE THANH_TOAN SET trangThai='CANCELLED',ngayCapNhat=SYSDATETIME() WHERE maDH=?", maDH);
            return null;
        });
    }

    public void processReturn(int maDH, String reason, int actorId) throws SQLException {
        processOrderIncident(maDH, "RETURN", "Trả hàng", reason, actorId, true);
    }

    public void processBombOrder(int maDH, String reason, int actorId) throws SQLException {
        processOrderIncident(maDH, "BOMB", "Bom hàng", reason, actorId, false);
    }

    private void processOrderIncident(int maDH, String incidentType, String status,
                                      String reason, int actorId, boolean refund) throws SQLException {
        String text = cleanNote(reason);
        inTransaction(entityManager -> {
            Map<String, Object> order = queryOne(entityManager,
                    "SELECT trangThai FROM DON_HANG WITH (UPDLOCK,ROWLOCK) WHERE maDH=?", maDH);
            if (order == null) throw new SQLException("Không tìm thấy đơn hàng.");
            restoreStockOnce(entityManager, maDH);
            executeUpdate(entityManager, """
                    UPDATE DON_HANG SET trangThai=?, loaiSuCo=?, lyDoSuCo=?, ngaySuCo=SYSDATETIME(),
                    ghiChu=CONCAT(ISNULL(ghiChu,N''),CASE WHEN ISNULL(ghiChu,N'')='' THEN N'' ELSE N' | ' END,?,N': ',?)
                    WHERE maDH=?
                    """, status, incidentType, text, status, text, maDH);
            if (refund) {
                executeUpdate(entityManager, """
                        UPDATE THANH_TOAN SET trangThai='CANCELLED',
                        ghiChuDoiSoat=CONCAT(ISNULL(ghiChuDoiSoat,N''),CASE WHEN ISNULL(ghiChuDoiSoat,N'')='' THEN N'' ELSE N' | ' END,N'Hoàn tiền/trả hàng: ',?),
                        ngayCapNhat=SYSDATETIME() WHERE maDH=?
                        """, text, maDH);
            } else {
                executeUpdate(entityManager, """
                        UPDATE THANH_TOAN SET trangThai=CASE WHEN trangThai='PAID' THEN trangThai ELSE 'CANCELLED' END,
                        ghiChuDoiSoat=CONCAT(ISNULL(ghiChuDoiSoat,N''),CASE WHEN ISNULL(ghiChuDoiSoat,N'')='' THEN N'' ELSE N' | ' END,N'Bom hàng: ',?),
                        ngayCapNhat=SYSDATETIME() WHERE maDH=?
                        """, text, maDH);
            }
            if (actorId > 0 && isStaff(entityManager, actorId)) {
                addStaffLog(entityManager, actorId, maDH, status, text);
            }
            return null;
        });
    }

    private void restoreStockOnce(EntityManager entityManager, int maDH) throws SQLException {
        Map<String, Object> order = queryOne(entityManager,
                "SELECT hangDaHoanKho FROM DON_HANG WITH (UPDLOCK,ROWLOCK) WHERE maDH=?", maDH);
        if (order == null) throw new SQLException("Không tìm thấy đơn hàng.");
        Object restoredValue = order.get("hangDaHoanKho");
        boolean restored = Boolean.TRUE.equals(restoredValue)
                || (restoredValue instanceof Number n && n.intValue() == 1);
        if (restored) return;
        executeUpdate(entityManager, """
                UPDATE sp SET sp.soLuongTon=sp.soLuongTon+ct.soLuong
                FROM SAN_PHAM sp
                JOIN CHI_TIET_DON_HANG ct ON ct.maSP=sp.maSP
                WHERE ct.maDH=?
                """, maDH);
        executeUpdate(entityManager, "UPDATE DON_HANG SET hangDaHoanKho=1 WHERE maDH=?", maDH);
    }

    public void reportOrderError(int maDH, String errorNote) throws SQLException {
        String text = cleanNote(errorNote);
        inTransaction(entityManager -> {
            Map<String,Object> row = queryOne(entityManager, """
                    SELECT dh.trangThai,yc.maYCTH
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    LEFT JOIN YEU_CAU_TRA_HANG yc ON yc.maDH=dh.maDH
                    WHERE dh.maDH=?
                    """, maDH);
            if (row == null) throw new IllegalStateException("Không tìm thấy đơn hàng.");
            if (row.get("maYCTH") != null) {
                throw new IllegalStateException("Đơn đang xử lý trả hàng/hoàn tiền nên không thể báo lỗi thanh toán.");
            }
            if ("Hoàn thành".equals(String.valueOf(row.get("trangThai")))) {
                throw new IllegalStateException("Đơn đã giao hoàn tất không thể chuyển sang báo lỗi thanh toán.");
            }
            executeUpdate(entityManager, """
                    UPDATE DON_HANG SET trangThai=N'Báo lỗi',
                    ghiChu=CONCAT(ISNULL(ghiChu,N''),CASE WHEN ISNULL(ghiChu,N'')='' THEN N'' ELSE N' | ' END,N'Báo lỗi: ',?)
                    WHERE maDH=?
                    """, text, maDH);
            executeUpdate(entityManager, "UPDATE THANH_TOAN SET trangThai='FAILED',ngayCapNhat=SYSDATETIME() WHERE maDH=?", maDH);
            return null;
        });
    }


    public void addDeliveryNote(int maDH, int staffId, String note) throws SQLException {
        if (note == null || note.isBlank()) return;
        String text = note.trim();
        executeUpdate("UPDATE DON_HANG SET ghiChu=CONCAT(ISNULL(ghiChu,N''),CASE WHEN ISNULL(ghiChu,N'')='' THEN N'' ELSE N' | ' END,N'Giao hàng: ',?) WHERE maDH=?", text, maDH);
        inTransaction(em -> { addStaffLog(em, staffId, maDH, "Ghi chú giao hàng", text); return null; });
    }

    public void updateLocation(int maDH, int staffId, double latitude, double longitude, String note) throws SQLException {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalStateException("Tọa độ không hợp lệ.");
        }
        inTransaction(entityManager -> {
            Map<String,Object> order = queryOne(entityManager, """
                    SELECT trangThai FROM DON_HANG WITH (UPDLOCK,ROWLOCK)
                    WHERE maDH=? AND maNhanVien=?
                    """, maDH, staffId);
            if (order == null) throw new IllegalStateException("Đơn hàng không thuộc shipper này.");
            String currentStatus = String.valueOf(order.get("trangThai"));
            if (!("Đang chuẩn bị".equals(currentStatus) || "Đang giao".equals(currentStatus))) {
                throw new IllegalStateException("Chỉ được gửi GPS khi đơn đã chuẩn bị xong hoặc đang giao.");
            }

            executeUpdate(entityManager, """
                    UPDATE DON_HANG SET viDoHienTai=?,kinhDoHienTai=?,capNhatViTri=SYSDATETIME()
                    WHERE maDH=? AND maNhanVien=?
                    """, latitude, longitude, maDH, staffId);
            String locationNote = cleanNote(note);
            executeUpdate(entityManager, """
                    INSERT INTO HANH_TRINH_DON_HANG(maDH,maNhanVien,viDo,kinhDo,ghiChu) VALUES(?,?,?,?,?)
                    """, maDH, staffId, latitude, longitude, locationNote);
            addStaffLog(entityManager, staffId, maDH, "Cập nhật vị trí",
                    locationNote + " (" + latitude + ", " + longitude + ")");
            return null;
        });
    }


    /**
     * Gửi cùng một điểm GPS cho toàn bộ đơn đang được shipper phụ trách.
     * Cách này giúp shipper dùng điện thoại chỉ cần bật một lần thay vì mở từng đơn.
     */
    public int updateLocationForActiveOrders(int staffId, double latitude, double longitude, String note) throws SQLException {
        List<Map<String, Object>> rows = query("""
                SELECT maDH FROM DON_HANG
                WHERE maNhanVien=? AND trangThai IN (N'Đang chuẩn bị',N'Đang giao')
                ORDER BY maDH
                """, staffId);
        int updated = 0;
        for (Map<String, Object> row : rows) {
            updateLocation(((Number) row.get("maDH")).intValue(), staffId, latitude, longitude, note);
            updated++;
        }
        return updated;
    }

    public List<Map<String, Object>> trackingHistory(int maDH) throws SQLException {
        return query("""
                SELECT TOP 50 ht.*,nv.hoTen AS tenNhanVien FROM HANH_TRINH_DON_HANG ht
                LEFT JOIN TAI_KHOAN nv ON nv.maTK=ht.maNhanVien WHERE ht.maDH=? ORDER BY ht.ngayCapNhat ASC
                """, maDH);
    }

    /** Số đơn mới đang chờ STAFF được phân công xử lý. */
    public int staffNewOrderCount(int staffId) throws SQLException {
        Map<String,Object> row = queryOne("""
                SELECT COUNT(*) AS total FROM DON_HANG
                WHERE maNhanVienXuLy=? AND trangThai=N'Chờ xác nhận'
                """, staffId);
        return row == null ? 0 : ((Number) row.get("total")).intValue();
    }

    /** Số đơn giao đang hoạt động của DELIVERY để hiển thị badge ở thanh bên trái. */
    public int deliveryActiveOrderCount(int deliveryId) throws SQLException {
        Map<String,Object> row = queryOne("""
                SELECT COUNT(*) AS total FROM DON_HANG
                WHERE maNhanVien=? AND trangThai IN (N'Đã xác nhận',N'Đang chuẩn bị',N'Đang giao')
                """, deliveryId);
        return row == null ? 0 : ((Number) row.get("total")).intValue();
    }

    public Map<String, Object> trackingData(int maDH) throws SQLException {
        return queryOne("""
                SELECT maDH,trangThai,diaChiNhan,viDoGiao,kinhDoGiao,viDoHienTai,kinhDoHienTai,capNhatViTri,maNhanVien
                FROM DON_HANG WHERE maDH=?
                """, maDH);
    }

    /**
     * Sửa dữ liệu demo cũ từng lấy trung điểm toán học giữa cửa hàng và khách.
     * Trung điểm đó có thể rơi sang Lào/Campuchia nên bản đồ nhìn như xe đi xuyên quốc gia.
     * Chỉ sửa các đơn mẫu có thời điểm GPS đúng bằng ngày đặt + 20 phút; GPS thật do shipper gửi không bị đụng tới.
     */
    public int normalizeDemoTrackingCoordinates() throws SQLException {
        return executeUpdate("""
                UPDATE DON_HANG
                SET viDoHienTai = CASE maTK
                        WHEN 6 THEN 10.8265000 WHEN 7 THEN 13.7829000 WHEN 8 THEN 10.3600000
                        WHEN 9 THEN 10.8350000 WHEN 10 THEN 16.4637000 WHEN 11 THEN 13.7829000
                        WHEN 12 THEN 11.9404000 WHEN 13 THEN 11.5486000 WHEN 14 THEN 18.6796000
                        WHEN 15 THEN 10.8650000 ELSE viDoHienTai END,
                    kinhDoHienTai = CASE maTK
                        WHEN 6 THEN 106.7035000 WHEN 7 THEN 109.2197000 WHEN 8 THEN 106.3600000
                        WHEN 9 THEN 106.7550000 WHEN 10 THEN 107.5909000 WHEN 11 THEN 109.2197000
                        WHEN 12 THEN 108.4583000 WHEN 13 THEN 107.8077000 WHEN 14 THEN 105.6813000
                        WHEN 15 THEN 106.7600000 ELSE kinhDoHienTai END
                WHERE trangThai=N'Đang giao'
                  AND ghiChu LIKE N'Dữ liệu mẫu tháng %'
                  AND capNhatViTri IS NOT NULL
                  AND ABS(DATEDIFF(SECOND,capNhatViTri,DATEADD(MINUTE,20,ngayDat))) <= 2
                  AND maTK BETWEEN 6 AND 15
                  AND (
                    ABS(ISNULL(viDoHienTai,0) - CASE maTK
                        WHEN 6 THEN 10.8265000 WHEN 7 THEN 13.7829000 WHEN 8 THEN 10.3600000
                        WHEN 9 THEN 10.8350000 WHEN 10 THEN 16.4637000 WHEN 11 THEN 13.7829000
                        WHEN 12 THEN 11.9404000 WHEN 13 THEN 11.5486000 WHEN 14 THEN 18.6796000
                        WHEN 15 THEN 10.8650000 ELSE ISNULL(viDoHienTai,0) END) > 0.0001
                    OR
                    ABS(ISNULL(kinhDoHienTai,0) - CASE maTK
                        WHEN 6 THEN 106.7035000 WHEN 7 THEN 109.2197000 WHEN 8 THEN 106.3600000
                        WHEN 9 THEN 106.7550000 WHEN 10 THEN 107.5909000 WHEN 11 THEN 109.2197000
                        WHEN 12 THEN 108.4583000 WHEN 13 THEN 107.8077000 WHEN 14 THEN 105.6813000
                        WHEN 15 THEN 106.7600000 ELSE ISNULL(kinhDoHienTai,0) END) > 0.0001
                  )
                """);
    }


    private boolean isStaff(EntityManager entityManager, int accountId) {
        return queryOne(entityManager,
                "SELECT maTK FROM TAI_KHOAN WHERE maTK=? AND vaiTro IN ('STAFF','DELIVERY')", accountId) != null;
    }

    private boolean isRole(EntityManager entityManager, int accountId, String role) {
        return queryOne(entityManager,
                "SELECT maTK FROM TAI_KHOAN WHERE maTK=? AND vaiTro=? AND trangThai=1", accountId, role) != null;
    }

    private boolean isBackOfficeActor(EntityManager entityManager, int accountId) {
        return queryOne(entityManager,
                "SELECT maTK FROM TAI_KHOAN WHERE maTK=? AND vaiTro IN ('ADMIN','STAFF','DELIVERY') AND trangThai=1",
                accountId) != null;
    }

    private void notifyAccount(EntityManager entityManager, int accountId, String title,
                               String content, String path, String type) {
        executeUpdate(entityManager, """
                INSERT INTO THONG_BAO_TAI_KHOAN(maTK,tieuDe,noiDung,duongDan,loai)
                VALUES(?,?,?,?,?)
                """, accountId, title, content, path, type);
    }

    private void addStaffLog(EntityManager entityManager, int staffId, Integer orderId,
                             String action, String content) {
        executeUpdate(entityManager, """
                INSERT INTO NHAT_KY_NHAN_VIEN(maNhanVien,maDH,hanhDong,noiDung)
                VALUES(?,?,?,?)
                """, staffId, orderId, action, content);
    }

    private String cleanNote(String note) {
        return note == null || note.isBlank() ? "Cập nhật vị trí giao hàng" : note.trim();
    }

    private List<Integer> parseIds(String[] values) {
        if (values == null) return Collections.emptyList();
        List<Integer> ids = new ArrayList<>();
        for (String value : values) {
            try { if (value != null && !value.isBlank()) ids.add(Integer.parseInt(value.trim())); }
            catch (NumberFormatException ignored) { }
        }
        return ids;
    }

    private String placeholders(int size) {
        return String.join(",", Collections.nCopies(size, "?"));
    }
}
