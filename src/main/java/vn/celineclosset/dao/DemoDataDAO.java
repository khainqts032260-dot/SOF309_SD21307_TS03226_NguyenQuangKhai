package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Bổ sung/khôi phục dữ liệu mẫu cho môi trường đồ án khi chạy trên database cũ.
 * Chỉ được gọi khi app.devMode=true. Mọi thao tác đều idempotent: restart không tạo trùng.
 */
public class DemoDataDAO extends CrudDAO {

    public int repairLegacyAssignments() throws SQLException {
        return inTransaction(entityManager -> {
            int changed = 0;

            // Các bản cũ từng dùng maNhanVien cho STAFF xử lý đơn. Chuyển đúng về maNhanVienXuLy.
            changed += executeUpdate(entityManager, """
                    UPDATE dh
                    SET dh.maNhanVienXuLy=dh.maNhanVien, dh.maNhanVien=NULL
                    FROM DON_HANG dh
                    JOIN TAI_KHOAN tk ON tk.maTK=dh.maNhanVien
                    WHERE dh.maNhanVienXuLy IS NULL AND tk.vaiTro='STAFF'
                    """);

            List<Map<String,Object>> staffs = query(entityManager, """
                    SELECT maTK FROM TAI_KHOAN
                    WHERE vaiTro='STAFF' AND trangThai=1
                    ORDER BY maTK
                    """);
            if (!staffs.isEmpty()) {
                List<Map<String,Object>> unassigned = query(entityManager, """
                        SELECT maDH FROM DON_HANG
                        WHERE maNhanVienXuLy IS NULL
                          AND trangThai NOT IN (N'Đã hủy',N'Bom hàng',N'Báo lỗi')
                        ORDER BY maDH
                        """);
                int index = 0;
                for (Map<String,Object> order : unassigned) {
                    int orderId = ((Number) order.get("maDH")).intValue();
                    int staffId = ((Number) staffs.get(index++ % staffs.size()).get("maTK")).intValue();
                    changed += executeUpdate(entityManager,
                            "UPDATE DON_HANG SET maNhanVienXuLy=? WHERE maDH=? AND maNhanVienXuLy IS NULL",
                            staffId, orderId);
                }
            }

            // maNhanVien chỉ dành cho DELIVERY. Xóa các tham chiếu vai trò sai còn sót lại.
            changed += executeUpdate(entityManager, """
                    UPDATE dh SET dh.maNhanVien=NULL
                    FROM DON_HANG dh
                    JOIN TAI_KHOAN tk ON tk.maTK=dh.maNhanVien
                    WHERE tk.vaiTro<>'DELIVERY'
                    """);

            List<Map<String,Object>> deliveries = query(entityManager, """
                    SELECT maTK FROM TAI_KHOAN
                    WHERE vaiTro='DELIVERY' AND trangThai=1
                    ORDER BY maTK
                    """);
            if (!deliveries.isEmpty()) {
                List<Map<String,Object>> missingDelivery = query(entityManager, """
                        SELECT maDH FROM DON_HANG
                        WHERE maNhanVien IS NULL
                          AND trangThai IN (N'Đang giao',N'Đã giao',N'Hoàn thành')
                        ORDER BY maDH
                        """);
                int index = 0;
                for (Map<String,Object> order : missingDelivery) {
                    int orderId = ((Number) order.get("maDH")).intValue();
                    int deliveryId = ((Number) deliveries.get(index++ % deliveries.size()).get("maTK")).intValue();
                    changed += executeUpdate(entityManager,
                            "UPDATE DON_HANG SET maNhanVien=? WHERE maDH=? AND maNhanVien IS NULL",
                            deliveryId, orderId);
                }
            }
            return changed;
        });
    }

    /**
     * Tạo doanh số mẫu cho sản phẩm chưa từng xuất hiện trong đơn Hoàn thành.
     * Nhờ đó trang chi tiết không còn hiển thị “Chưa có lượt mua” cho sản phẩm demo mới.
     */
    public int ensureDemoSalesForUnsoldProducts() throws SQLException {
        return inTransaction(entityManager -> {
            List<Map<String,Object>> products = query(entityManager, """
                    SELECT sp.maSP,sp.donGia
                    FROM SAN_PHAM sp
                    WHERE sp.trangThai=1
                      AND NOT EXISTS(
                        SELECT 1 FROM CHI_TIET_DON_HANG ct
                        JOIN DON_HANG dh ON dh.maDH=ct.maDH
                        WHERE ct.maSP=sp.maSP AND dh.trangThai=N'Hoàn thành'
                      )
                    ORDER BY sp.maSP
                    """);
            if (products.isEmpty()) return 0;

            List<Map<String,Object>> customers = query(entityManager, """
                    SELECT TOP 10 maTK,hoTen,soDienThoai,diaChiMacDinh
                    FROM TAI_KHOAN WHERE vaiTro='CUSTOMER' AND trangThai=1 ORDER BY maTK
                    """);
            List<Map<String,Object>> staffs = query(entityManager,
                    "SELECT maTK FROM TAI_KHOAN WHERE vaiTro='STAFF' AND trangThai=1 ORDER BY maTK");
            List<Map<String,Object>> deliveries = query(entityManager,
                    "SELECT maTK FROM TAI_KHOAN WHERE vaiTro='DELIVERY' AND trangThai=1 ORDER BY maTK");
            if (customers.isEmpty() || staffs.isEmpty() || deliveries.isEmpty()) return 0;

            int created = 0;
            int index = 0;
            for (Map<String,Object> product : products) {
                int productId = ((Number) product.get("maSP")).intValue();
                if (queryOne(entityManager,
                        "SELECT TOP 1 maDH FROM DON_HANG WHERE ghiChu=?",
                        "DEMO_AUTO_SALE:" + productId) != null) {
                    continue;
                }

                BigDecimal price = new BigDecimal(String.valueOf(product.get("donGia")));
                int quantity = 2 + (productId % 4); // 2..5 lượt mẫu cho sản phẩm chưa có doanh số.
                BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
                Map<String,Object> customer = customers.get(index % customers.size());
                int staffId = ((Number) staffs.get(index % staffs.size()).get("maTK")).intValue();
                int deliveryId = ((Number) deliveries.get(index % deliveries.size()).get("maTK")).intValue();
                index++;

                LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 1, 9, 0)
                        .plusDays(productId % 25L);
                LocalDateTime completedAt = orderedAt.plusDays(2);
                int points = total.divideToIntegralValue(BigDecimal.valueOf(10000)).intValue();

                Map<String,Object> inserted = queryOne(entityManager, """
                        INSERT INTO DON_HANG(
                            ngayDat,tongTien,tienGiam,trangThai,phuongThucThanhToan,
                            maTK,maNhanVien,maNhanVienXuLy,ngayXacNhan,ngayHoanThanh,
                            hoTenNhan,soDienThoaiNhan,diaChiNhan,ghiChu,diemCong,daCongDiem
                        )
                        OUTPUT INSERTED.maDH AS maDH
                        VALUES(?,?,0,N'Hoàn thành','COD',?,?,?,?,?,?,?,?,?,?,1)
                        """,
                        orderedAt,total,
                        ((Number) customer.get("maTK")).intValue(),deliveryId,staffId,
                        orderedAt.plusHours(1),completedAt,
                        String.valueOf(customer.get("hoTen")),
                        safeText(customer.get("soDienThoai"), "0900000000"),
                        safeText(customer.get("diaChiMacDinh"), "TP.HCM, Việt Nam"),
                        "DEMO_AUTO_SALE:" + productId,points);
                if (inserted == null) continue;
                int orderId = ((Number) inserted.get("maDH")).intValue();

                executeUpdate(entityManager, """
                        INSERT INTO CHI_TIET_DON_HANG(maDH,maSP,soLuong,donGia,thanhTien)
                        VALUES(?,?,?,?,?)
                        """, orderId,productId,quantity,price,total);
                executeUpdate(entityManager, """
                        INSERT INTO THANH_TOAN(maDH,phuongThuc,soTien,soTienDaNhan,noiDungChuyenKhoan,
                                              ngayThanhToan,trangThai,trangThaiDoiSoat,ghiChuDoiSoat)
                        VALUES(?,'COD',?,?,'DEMO',?,'PAID','NONE',N'Dữ liệu mua mẫu cho đồ án')
                        """, orderId,total,total,completedAt);
                created++;
            }
            return created;
        });
    }

    /** Sửa các phiếu hỗ trợ cũ bị gán nhầm sang DELIVERY và tạo message từ nội dung phiếu. */
    public int repairSupportDemoData() throws SQLException {
        return inTransaction(entityManager -> {
            List<Map<String,Object>> staffs = query(entityManager,
                    "SELECT maTK FROM TAI_KHOAN WHERE vaiTro='STAFF' AND trangThai=1 ORDER BY maTK");
            if (staffs.isEmpty()) return 0;
            int changed = 0;

            List<Map<String,Object>> requests = query(entityManager, """
                    SELECT yc.maYC,yc.maNhanVien
                    FROM YEU_CAU_HO_TRO yc
                    LEFT JOIN TAI_KHOAN tk ON tk.maTK=yc.maNhanVien
                    WHERE yc.maNhanVien IS NULL OR tk.vaiTro<>'STAFF' OR tk.trangThai<>1
                    ORDER BY yc.maYC
                    """);
            int index=0;
            for (Map<String,Object> request : requests) {
                int requestId=((Number)request.get("maYC")).intValue();
                int staffId=((Number)staffs.get(index++ % staffs.size()).get("maTK")).intValue();
                changed += executeUpdate(entityManager,
                        "UPDATE YEU_CAU_HO_TRO SET maNhanVien=?,trangThai=CASE WHEN trangThai='MOI' THEN 'DANG_XU_LY' ELSE trangThai END WHERE maYC=?",
                        staffId,requestId);
            }

            // Phiếu seed cũ chỉ có noiDung/phanHoi ở bảng cha nên khung chat có thể rỗng.
            changed += executeUpdate(entityManager, """
                    INSERT INTO TIN_NHAN_HO_TRO(maYC,maNguoiGui,vaiTroNguoiGui,noiDung,ngayGui,daDoc)
                    SELECT yc.maYC,yc.maTK,'CUSTOMER',yc.noiDung,yc.ngayTao,0
                    FROM YEU_CAU_HO_TRO yc
                    WHERE yc.maTK IS NOT NULL AND NULLIF(LTRIM(RTRIM(yc.noiDung)),N'') IS NOT NULL
                      AND NOT EXISTS(SELECT 1 FROM TIN_NHAN_HO_TRO tn WHERE tn.maYC=yc.maYC AND tn.vaiTroNguoiGui='CUSTOMER')
                    """);
            changed += executeUpdate(entityManager, """
                    INSERT INTO TIN_NHAN_HO_TRO(maYC,maNguoiGui,vaiTroNguoiGui,noiDung,ngayGui,daDoc)
                    SELECT yc.maYC,yc.maNhanVien,'STAFF',yc.phanHoi,yc.ngayCapNhat,1
                    FROM YEU_CAU_HO_TRO yc
                    WHERE yc.maNhanVien IS NOT NULL AND NULLIF(LTRIM(RTRIM(yc.phanHoi)),N'') IS NOT NULL
                      AND NOT EXISTS(SELECT 1 FROM TIN_NHAN_HO_TRO tn WHERE tn.maYC=yc.maYC AND tn.vaiTroNguoiGui='STAFF')
                    """);
            return changed;
        });
    }
    private String safeText(Object value, String fallback) {
        if (value == null) return fallback;
        String text = String.valueOf(value).trim();
        return text.isEmpty() || "null".equalsIgnoreCase(text) ? fallback : text;
    }

}
