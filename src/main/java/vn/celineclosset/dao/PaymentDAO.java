package vn.celineclosset.dao;

import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import vn.celineclosset.payment.PaymentResult;
import vn.celineclosset.payment.SePayWebhookPayload;
import vn.celineclosset.service.BankTransferService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** DAO riêng cho QR chuyển khoản và đối chiếu giao dịch SePay. */
public class PaymentDAO extends CrudDAO {
    private static final Gson GSON = new Gson();

    public Map<String, Object> paymentForCustomer(int customerId, int orderId) throws SQLException {
        return queryOne("""
                SELECT dh.maDH,dh.maTK,dh.ngayDat,dh.tongTien,dh.phuongThucThanhToan,dh.trangThai AS trangThaiDon,
                       tt.maTT,tt.phuongThuc,tt.soTien,tt.noiDungChuyenKhoan,tt.ngayThanhToan,
                       tt.trangThai,tt.soTienDaNhan,tt.trangThaiDoiSoat,tt.ghiChuDoiSoat,
                       tt.maGiaoDichNganHang,tt.maGiaoDichSePay,tt.ngayCapNhat
                FROM DON_HANG dh
                JOIN THANH_TOAN tt ON tt.maDH=dh.maDH
                WHERE dh.maTK=? AND dh.maDH=?
                """, customerId, orderId);
    }


    public List<Map<String, Object>> paymentItemsForCustomer(int customerId, int orderId) throws SQLException {
        return query("""
                SELECT ctdh.maSP,ctdh.soLuong,ctdh.donGia,ctdh.thanhTien,ctdh.mauSac,ctdh.kichThuoc,
                       sp.tenSP,COALESCE(img.duongDan,sp.hinhAnh) AS hinhAnh
                FROM DON_HANG dh
                JOIN CHI_TIET_DON_HANG ctdh ON ctdh.maDH=dh.maDH
                JOIN SAN_PHAM sp ON sp.maSP=ctdh.maSP
                OUTER APPLY (
                    SELECT TOP 1 ha.duongDan
                    FROM HINH_ANH_SAN_PHAM ha
                    WHERE ha.maSP=sp.maSP
                    ORDER BY ha.thuTu,ha.maAnh
                ) img
                WHERE dh.maTK=? AND dh.maDH=?
                ORDER BY ctdh.maCTDH
                """, customerId, orderId);
    }

    /** Mở lại một phiên QR đã hủy khi khách chủ động quay lại từ trang đơn hàng. */
    public boolean resumeCancelledBankPayment(int customerId, int orderId) throws SQLException {
        int changed = executeUpdate("""
                UPDATE tt
                SET tt.trangThai='PENDING',
                    tt.trangThaiDoiSoat=CASE WHEN tt.soTienDaNhan>0 THEN 'UNDERPAID' ELSE 'NONE' END,
                    tt.ghiChuDoiSoat=CASE WHEN tt.soTienDaNhan>0
                        THEN CONCAT(N'Khách mở lại thanh toán; đã nhận ',CONVERT(VARCHAR(30),tt.soTienDaNhan),N'đ')
                        ELSE N'Khách mở lại QR thanh toán' END,
                    tt.ngayCapNhat=SYSDATETIME()
                FROM THANH_TOAN tt
                JOIN DON_HANG dh ON dh.maDH=tt.maDH
                WHERE dh.maTK=? AND tt.maDH=? AND tt.phuongThuc='BANK' AND tt.trangThai='CANCELLED'
                """, customerId, orderId);
        return changed > 0;
    }

    /**
     * Đánh dấu phiên thanh toán QR đã bị khách rời khỏi. Chỉ hủy phiên thanh toán,
     * không hủy đơn hàng. Webhook tiền vào vẫn có quyền chuyển trạng thái sang PAID.
     */
    public boolean cancelPendingBankPayment(int customerId, int orderId, String reason) throws SQLException {
        String safeReason = trimTo(reason, 220);
        if (safeReason.isBlank()) safeReason = "Khách rời trang thanh toán trước khi SePay xác nhận";
        int changed = executeUpdate("""
                UPDATE tt
                SET tt.trangThai='CANCELLED',
                    tt.trangThaiDoiSoat=CASE WHEN tt.soTienDaNhan>0 THEN 'REVIEW' ELSE tt.trangThaiDoiSoat END,
                    tt.ghiChuDoiSoat=CASE WHEN tt.soTienDaNhan>0
                        THEN CONCAT(N'Phiên thanh toán đã hủy sau khi nhận ',CONVERT(VARCHAR(30),tt.soTienDaNhan),N'đ. ',?)
                        ELSE ? END,
                    tt.ngayCapNhat=SYSDATETIME()
                FROM THANH_TOAN tt
                JOIN DON_HANG dh ON dh.maDH=tt.maDH
                WHERE dh.maTK=? AND tt.maDH=? AND tt.phuongThuc='BANK' AND tt.trangThai='PENDING'
                """, safeReason, safeReason, customerId, orderId);
        return changed > 0;
    }

    /**
     * Khách bấm Hủy ở trang QR trước khi có tiền vào: rollback toàn bộ đơn tạm để quay lại checkout.
     * Không rollback nếu SePay đã ghi nhận giao dịch/đã nhận tiền, nhằm tránh xóa dấu vết tài chính.
     */
    public CheckoutRollbackResult rollbackPendingBankCheckout(int customerId, int orderId) throws SQLException {
        return inTransaction(entityManager -> {
            Map<String, Object> order = queryOne(entityManager, """
                    SELECT dh.maDH,dh.maTK,dh.maVoucher,dh.trangThai AS trangThaiDon,
                           COALESCE(v.maCode,'') AS maVoucherCode,
                           tt.trangThai AS trangThaiThanhToan,COALESCE(tt.soTienDaNhan,0) AS soTienDaNhan
                    FROM DON_HANG dh WITH (UPDLOCK,HOLDLOCK)
                    JOIN THANH_TOAN tt WITH (UPDLOCK,HOLDLOCK) ON tt.maDH=dh.maDH
                    LEFT JOIN VOUCHER v ON v.maVoucher=dh.maVoucher
                    WHERE dh.maTK=? AND dh.maDH=? AND tt.phuongThuc='BANK'
                    """, customerId, orderId);
            if (order == null) {
                return new CheckoutRollbackResult(false, List.of(), "",
                        "Không tìm thấy phiên thanh toán chuyển khoản của bạn.");
            }

            String paymentStatus = clean(String.valueOf(order.get("trangThaiThanhToan")));
            String orderStatus = clean(String.valueOf(order.get("trangThaiDon")));
            BigDecimal received = money(order.get("soTienDaNhan"));
            if (!"PENDING".equalsIgnoreCase(paymentStatus)
                    || !"Chờ xác nhận".equalsIgnoreCase(orderStatus)
                    || received.signum() > 0) {
                return new CheckoutRollbackResult(false, List.of(), clean(String.valueOf(order.get("maVoucherCode"))),
                        "Không thể quay lại checkout vì thanh toán/đơn hàng đã được xử lý hoặc đã ghi nhận tiền.");
            }

            Map<String, Object> sepay = queryOne(entityManager,
                    "SELECT TOP 1 maGiaoDichSePay FROM GIAO_DICH_SEPAY WITH (UPDLOCK,HOLDLOCK) WHERE maDH=?", orderId);
            if (sepay != null) {
                return new CheckoutRollbackResult(false, List.of(), clean(String.valueOf(order.get("maVoucherCode"))),
                        "SePay đã ghi nhận giao dịch liên quan. Hệ thống giữ đơn để đối soát an toàn.");
            }

            List<Map<String, Object>> items = query(entityManager, """
                    SELECT maSP,soLuong,donGia,mauSac,kichThuoc
                    FROM CHI_TIET_DON_HANG WITH (UPDLOCK,HOLDLOCK)
                    WHERE maDH=? ORDER BY maCTDH
                    """, orderId);

            Map<String, Object> cart = queryOne(entityManager,
                    "SELECT TOP 1 maGH FROM GIO_HANG WITH (UPDLOCK,HOLDLOCK) WHERE maTK=? AND trangThai=1 ORDER BY maGH DESC",
                    customerId);
            int cartId;
            if (cart == null) {
                Map<String, Object> created = queryOne(entityManager,
                        "INSERT INTO GIO_HANG(maTK,trangThai) OUTPUT INSERTED.maGH AS maGH VALUES(?,1)", customerId);
                if (created == null || created.get("maGH") == null) throw new SQLException("Không khôi phục được giỏ hàng");
                cartId = ((Number) created.get("maGH")).intValue();
            } else {
                cartId = ((Number) cart.get("maGH")).intValue();
            }

            List<Integer> restoredCartItemIds = new ArrayList<>();
            for (Map<String, Object> item : items) {
                int productId = ((Number) item.get("maSP")).intValue();
                int quantity = ((Number) item.get("soLuong")).intValue();
                BigDecimal unitPrice = money(item.get("donGia"));
                String color = item.get("mauSac") == null ? null : String.valueOf(item.get("mauSac"));
                String size = item.get("kichThuoc") == null ? null : String.valueOf(item.get("kichThuoc"));

                // Trả tồn kho trước vì createOrder đã giữ số lượng ngay lúc tạo QR.
                executeUpdate(entityManager, "UPDATE SAN_PHAM SET soLuongTon=soLuongTon+? WHERE maSP=?", quantity, productId);

                Map<String, Object> existing = queryOne(entityManager, """
                        SELECT maCTGH,soLuong FROM CHI_TIET_GIO_HANG WITH (UPDLOCK,HOLDLOCK)
                        WHERE maGH=? AND maSP=?
                          AND ISNULL(mauSac,'')=ISNULL(?,'')
                          AND ISNULL(kichThuoc,'')=ISNULL(?,'')
                        """, cartId, productId, color, size);
                int cartItemId;
                if (existing == null) {
                    Map<String, Object> createdItem = queryOne(entityManager, """
                            INSERT INTO CHI_TIET_GIO_HANG(maGH,maSP,soLuong,donGia,giamGia,thanhTien,mauSac,kichThuoc)
                            OUTPUT INSERTED.maCTGH AS maCTGH
                            VALUES(?,?,?,?,0,?,?,?)
                            """, cartId, productId, quantity, unitPrice,
                            unitPrice.multiply(BigDecimal.valueOf(quantity)), color, size);
                    if (createdItem == null || createdItem.get("maCTGH") == null) {
                        throw new SQLException("Không khôi phục được sản phẩm vào giỏ hàng");
                    }
                    cartItemId = ((Number) createdItem.get("maCTGH")).intValue();
                } else {
                    cartItemId = ((Number) existing.get("maCTGH")).intValue();
                    int newQty = ((Number) existing.get("soLuong")).intValue() + quantity;
                    executeUpdate(entityManager, """
                            UPDATE CHI_TIET_GIO_HANG
                            SET soLuong=?,donGia=?,giamGia=0,thanhTien=?
                            WHERE maCTGH=?
                            """, newQty, unitPrice, unitPrice.multiply(BigDecimal.valueOf(newQty)), cartItemId);
                }
                restoredCartItemIds.add(cartItemId);
            }

            Object voucherValue = order.get("maVoucher");
            if (voucherValue instanceof Number voucherNumber) {
                int voucherId = voucherNumber.intValue();
                executeUpdate(entityManager,
                        "UPDATE VOUCHER SET daDung=CASE WHEN daDung>0 THEN daDung-1 ELSE 0 END WHERE maVoucher=?",
                        voucherId);
                executeUpdate(entityManager, """
                        UPDATE KHACH_HANG_VOUCHER
                        SET trangThai='AVAILABLE',ngaySuDung=NULL,maDH=NULL
                        WHERE maTK=? AND maVoucher=? AND maDH=? AND trangThai='USED'
                        """, customerId, voucherId, orderId);
            }

            // Dọn các bảng phụ nếu có dữ liệu phát sinh ngoài ý muốn trước khi khách bấm Hủy.
            executeUpdate(entityManager, "DELETE FROM PHAN_HOI WHERE maDH=?", orderId);
            executeUpdate(entityManager, "DELETE FROM LICH_SU_DIEM WHERE maDH=?", orderId);
            executeUpdate(entityManager, "DELETE FROM HANH_TRINH_DON_HANG WHERE maDH=?", orderId);
            executeUpdate(entityManager, "DELETE FROM NHAT_KY_NHAN_VIEN WHERE maDH=?", orderId);
            executeUpdate(entityManager, """
                    DELETE l FROM LAN_GIAO_THAT_BAI l
                    JOIN YEU_CAU_GIAO_THAT_BAI y ON y.maYCGTB=l.maYCGTB
                    WHERE y.maDH=?
                    """, orderId);
            executeUpdate(entityManager, "DELETE FROM YEU_CAU_GIAO_THAT_BAI WHERE maDH=?", orderId);
            executeUpdate(entityManager, "DELETE FROM YEU_CAU_TRA_HANG WHERE maDH=?", orderId);
            executeUpdate(entityManager, "DELETE FROM THANH_TOAN WHERE maDH=?", orderId);
            executeUpdate(entityManager, "DELETE FROM CHI_TIET_DON_HANG WHERE maDH=?", orderId);
            executeUpdate(entityManager,
                    "DELETE FROM THONG_BAO_TAI_KHOAN WHERE loai='NEW_ORDER' AND duongDan=?",
                    "/admin/order-detail?id=" + orderId);
            int deleted = executeUpdate(entityManager,
                    "DELETE FROM DON_HANG WHERE maDH=? AND maTK=? AND trangThai=N'Chờ xác nhận'",
                    orderId, customerId);
            if (deleted == 0) throw new SQLException("Đơn tạm đã thay đổi trạng thái, không thể rollback checkout");

            return new CheckoutRollbackResult(true, List.copyOf(restoredCartItemIds),
                    clean(String.valueOf(order.get("maVoucherCode"))),
                    "Đã quay lại checkout. Đơn tạm và phiên QR vừa tạo đã được xóa.");
        });
    }

    public record CheckoutRollbackResult(boolean rolledBack, List<Integer> restoredCartItemIds,
                                         String voucherCode, String message) {}

    /** Chuẩn hóa mã chuyển khoản của toàn bộ đơn BANK, ví dụ đơn 45 thành DH00045. */
    public int normalizeBankPaymentCodes() throws SQLException {
        BankTransferService service = new BankTransferService();
        List<Map<String, Object>> payments = query("SELECT maDH,noiDungChuyenKhoan FROM THANH_TOAN WHERE phuongThuc='BANK'");
        int changed = 0;
        for (Map<String, Object> payment : payments) {
            int orderId = ((Number) payment.get("maDH")).intValue();
            String expectedCode = service.paymentCode(orderId);
            String currentCode = clean(String.valueOf(payment.get("noiDungChuyenKhoan")));
            if (!expectedCode.equalsIgnoreCase(currentCode)) {
                changed += executeUpdate("UPDATE THANH_TOAN SET noiDungChuyenKhoan=?,ngayCapNhat=SYSDATETIME() WHERE maDH=? AND phuongThuc='BANK'",
                        expectedCode, orderId);
            }
        }
        return changed;
    }

    public long paymentSecondsRemaining(int customerId, int orderId, int expirationMinutes) throws SQLException {
        int safeMinutes = Math.max(1, Math.min(24 * 60, expirationMinutes));
        Map<String, Object> row = queryOne("""
                SELECT CASE
                    WHEN DATEADD(MINUTE,?,dh.ngayDat)<=SYSDATETIME() THEN 0
                    ELSE DATEDIFF(SECOND,SYSDATETIME(),DATEADD(MINUTE,?,dh.ngayDat))
                END AS secondsRemaining
                FROM DON_HANG dh
                JOIN THANH_TOAN tt ON tt.maDH=dh.maDH
                WHERE dh.maTK=? AND dh.maDH=? AND tt.phuongThuc='BANK'
                """, safeMinutes, safeMinutes, customerId, orderId);
        if (row == null || !(row.get("secondsRemaining") instanceof Number number)) return 0;
        return Math.max(0L, number.longValue());
    }

    /** Đảm bảo đơn đang mở QR luôn dùng mã đã đệm đủ số. */
    public void ensurePaymentCodeForCustomer(int customerId, int orderId, String paymentCode) throws SQLException {
        executeUpdate("""
                UPDATE tt
                SET tt.noiDungChuyenKhoan=?,tt.ngayCapNhat=SYSDATETIME()
                FROM THANH_TOAN tt
                JOIN DON_HANG dh ON dh.maDH=tt.maDH
                WHERE dh.maTK=? AND tt.maDH=? AND tt.phuongThuc='BANK'
                """, paymentCode, customerId, orderId);
    }

    /**
     * Hủy đơn BANK chưa nhận đồng nào sau thời gian chờ. Chạy được cả khi khách đã đóng trang.
     * Nếu đã nhận một phần tiền thì không tự hủy, để STAFF đối soát an toàn.
     */
    public int expirePendingBankPayments(int expirationMinutes) throws SQLException {
        int safeMinutes = Math.max(1, Math.min(24 * 60, expirationMinutes));
        return inTransaction(entityManager -> {
            List<Map<String, Object>> expired = query(entityManager, """
                    SELECT dh.maDH,dh.maVoucher,dh.maTK
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    JOIN THANH_TOAN tt WITH (UPDLOCK,ROWLOCK) ON tt.maDH=dh.maDH
                    WHERE tt.phuongThuc='BANK'
                      AND tt.trangThai='PENDING'
                      AND COALESCE(tt.soTienDaNhan,0)=0
                      AND dh.trangThai=N'Chờ xác nhận'
                      AND DATEADD(MINUTE,?,dh.ngayDat)<=SYSDATETIME()
                    """, safeMinutes);

            int cancelled = 0;
            for (Map<String, Object> row : expired) {
                int orderId = ((Number) row.get("maDH")).intValue();
                int orderChanged = executeUpdate(entityManager, """
                        UPDATE dh
                        SET dh.trangThai=N'Đã hủy',dh.lyDoHuy=N'Thanh toán chuyển khoản không thành công sau thời gian chờ',
                            dh.nguoiHuy='SYSTEM',dh.ngayHuy=SYSDATETIME(),
                            dh.ghiChu=CONCAT(ISNULL(dh.ghiChu,N''),CASE WHEN ISNULL(dh.ghiChu,N'')='' THEN N'' ELSE N' | ' END,
                                N'Hệ thống tự hủy: chưa nhận thanh toán sau ',?,N' phút')
                        FROM DON_HANG dh
                        JOIN THANH_TOAN tt ON tt.maDH=dh.maDH
                        WHERE dh.maDH=? AND dh.trangThai=N'Chờ xác nhận'
                          AND tt.phuongThuc='BANK' AND tt.trangThai='PENDING' AND COALESCE(tt.soTienDaNhan,0)=0
                        """, safeMinutes, orderId);
                if (orderChanged == 0) continue;

                executeUpdate(entityManager, """
                        UPDATE THANH_TOAN
                        SET trangThai='FAILED',trangThaiDoiSoat='NONE',
                            ghiChuDoiSoat=?,ngayCapNhat=SYSDATETIME()
                        WHERE maDH=? AND trangThai='PENDING'
                        """, "Thanh toán không thành công: quá " + safeMinutes + " phút chưa nhận được tiền", orderId);

                executeUpdate(entityManager, """
                        UPDATE sp
                        SET sp.soLuongTon=sp.soLuongTon+ct.soLuong
                        FROM SAN_PHAM sp
                        JOIN CHI_TIET_DON_HANG ct ON ct.maSP=sp.maSP
                        WHERE ct.maDH=?
                        """, orderId);

                Object voucherValue = row.get("maVoucher");
                if (voucherValue instanceof Number voucherNumber) {
                    int voucherId = voucherNumber.intValue();
                    executeUpdate(entityManager,
                            "UPDATE VOUCHER SET daDung=CASE WHEN daDung>0 THEN daDung-1 ELSE 0 END WHERE maVoucher=?",
                            voucherId);
                    executeUpdate(entityManager, """
                            UPDATE KHACH_HANG_VOUCHER
                            SET trangThai='AVAILABLE',ngaySuDung=NULL,maDH=NULL
                            WHERE maTK=? AND maVoucher=? AND maDH=? AND trangThai='USED'
                            """, ((Number) row.get("maTK")).intValue(), voucherId, orderId);
                }
                cancelled++;
            }
            return cancelled;
        });
    }

    public PaymentResult processSePayWebhook(SePayWebhookPayload payload, String expectedAccount,
                                              String paymentPrefix) throws SQLException {
        if (payload == null || payload.getId() <= 0) {
            return new PaymentResult(false, false, null, null, null, "Thiếu mã giao dịch SePay.");
        }
        if (!"in".equalsIgnoreCase(clean(payload.getTransferType()))) {
            return storeUnmatched(payload, null, "IGNORED_OUT", "Không phải giao dịch tiền vào.");
        }
        BigDecimal transferAmount = money(payload.getTransferAmount());
        if (transferAmount.signum() <= 0) {
            return storeUnmatched(payload, null, "INVALID_AMOUNT", "Số tiền giao dịch không hợp lệ.");
        }

        Integer orderId = extractOrderId(payload, paymentPrefix);
        String configuredAccount = normalizeAccount(expectedAccount);
        String incomingAccount = normalizeAccount(payload.getAccountNumber());
        if (!configuredAccount.isBlank() && !configuredAccount.equals(incomingAccount)) {
            return storeUnmatched(payload, orderId, "WRONG_ACCOUNT", "Giao dịch không vào đúng tài khoản nhận đã cấu hình.");
        }

        return inTransaction(entityManager -> {
            Map<String, Object> duplicate = queryOne(entityManager,
                    "SELECT maGiaoDichSePay FROM GIAO_DICH_SEPAY WITH (UPDLOCK,HOLDLOCK) WHERE maGiaoDichSePay=?",
                    payload.getId());
            if (duplicate != null) {
                return new PaymentResult(true, true, orderId, null, null, "Giao dịch đã được xử lý trước đó.");
            }

            if (orderId == null) {
                insertTransaction(entityManager, payload, null, "UNMATCHED_CODE", "Không tìm thấy mã đơn trong nội dung giao dịch.");
                return new PaymentResult(true, false, null, null, null, "Đã lưu giao dịch chưa ghép được đơn hàng.");
            }

            Map<String, Object> payment = queryOne(entityManager, """
                    SELECT tt.maTT,tt.maDH,tt.phuongThuc,tt.soTien,tt.soTienDaNhan,tt.trangThai,
                           dh.trangThai AS trangThaiDon
                    FROM THANH_TOAN tt WITH (UPDLOCK,ROWLOCK)
                    JOIN DON_HANG dh ON dh.maDH=tt.maDH
                    WHERE tt.maDH=?
                    """, orderId);
            if (payment == null) {
                insertTransaction(entityManager, payload, null, "ORDER_NOT_FOUND", "Không tìm thấy thanh toán của đơn.");
                return new PaymentResult(true, false, orderId, null, null, "Không tìm thấy thanh toán của đơn.");
            }
            if (!"BANK".equalsIgnoreCase(String.valueOf(payment.get("phuongThuc")))) {
                insertTransaction(entityManager, payload, orderId, "WRONG_METHOD", "Đơn không chọn chuyển khoản ngân hàng.");
                return new PaymentResult(true, false, orderId, String.valueOf(payment.get("trangThai")), null,
                        "Đơn không dùng phương thức chuyển khoản.");
            }
            if ("Đã hủy".equals(String.valueOf(payment.get("trangThaiDon")))) {
                insertTransaction(entityManager, payload, orderId, "CANCELLED_ORDER", "Tiền vào sau khi đơn đã hủy.");
                executeUpdate(entityManager, """
                        UPDATE THANH_TOAN
                        SET soTienDaNhan=soTienDaNhan+?,trangThaiDoiSoat='REVIEW',
                            ghiChuDoiSoat=N'Nhận tiền sau khi đơn đã hủy',maGiaoDichNganHang=?,
                            maGiaoDichSePay=?,ngayCapNhat=SYSDATETIME()
                        WHERE maDH=?
                        """, transferAmount, trimTo(payload.getReferenceCode(), 120), payload.getId(), orderId);
                return new PaymentResult(true, false, orderId, String.valueOf(payment.get("trangThai")), "REVIEW",
                        "Đơn đã hủy, cần nhân viên đối soát.");
            }

            BigDecimal expected = money(payment.get("soTien"));
            BigDecimal receivedBefore = money(payment.get("soTienDaNhan"));
            BigDecimal receivedAfter = receivedBefore.add(transferAmount);
            String previousStatus = String.valueOf(payment.get("trangThai"));
            boolean wasCancelled = "CANCELLED".equalsIgnoreCase(previousStatus);
            boolean fullyPaid = receivedAfter.compareTo(expected) >= 0;
            String paymentStatus = fullyPaid ? "PAID" : (wasCancelled ? "CANCELLED" : "PENDING");
            String reconciliation;
            if (wasCancelled) reconciliation = "REVIEW";
            else if (receivedAfter.compareTo(expected) < 0) reconciliation = "UNDERPAID";
            else if (receivedAfter.compareTo(expected) > 0) reconciliation = "OVERPAID";
            else reconciliation = "NONE";

            String note;
            if (wasCancelled && fullyPaid) {
                note = "Đã nhận đủ tiền sau khi khách rời trang thanh toán; cần lưu ý khi đối soát";
            } else if (wasCancelled) {
                note = "Đã nhận " + receivedAfter.toPlainString() + "đ sau khi phiên thanh toán bị hủy; cần STAFF đối soát";
            } else {
                note = switch (reconciliation) {
                    case "UNDERPAID" -> "Đã nhận một phần, còn thiếu " + expected.subtract(receivedAfter).toPlainString() + "đ";
                    case "OVERPAID" -> "Khách chuyển thừa " + receivedAfter.subtract(expected).toPlainString() + "đ";
                    default -> "Đã nhận đủ tiền qua SePay";
                };
            }

            executeUpdate(entityManager, """
                    UPDATE THANH_TOAN
                    SET soTienDaNhan=?,trangThai=?,trangThaiDoiSoat=?,ghiChuDoiSoat=?,
                        ngayThanhToan=CASE WHEN ?='PAID' THEN COALESCE(ngayThanhToan,SYSDATETIME()) ELSE NULL END,
                        maGiaoDichNganHang=?,maGiaoDichSePay=?,ngayCapNhat=SYSDATETIME()
                    WHERE maDH=?
                    """, receivedAfter, paymentStatus, reconciliation, note, paymentStatus,
                    trimTo(payload.getReferenceCode(), 120), payload.getId(), orderId);
            insertTransaction(entityManager, payload, orderId, paymentStatus, note);

            return new PaymentResult(true, false, orderId, paymentStatus, reconciliation, note);
        });
    }

    private PaymentResult storeUnmatched(SePayWebhookPayload payload, Integer orderId,
                                         String processStatus, String note) throws SQLException {
        return inTransaction(entityManager -> {
            Map<String, Object> duplicate = queryOne(entityManager,
                    "SELECT maGiaoDichSePay FROM GIAO_DICH_SEPAY WITH (UPDLOCK,HOLDLOCK) WHERE maGiaoDichSePay=?",
                    payload.getId());
            if (duplicate != null) {
                return new PaymentResult(true, true, orderId, null, null, "Giao dịch đã được lưu trước đó.");
            }
            Integer linkedOrderId = orderId;
            if (linkedOrderId != null && queryOne(entityManager,
                    "SELECT maDH FROM DON_HANG WHERE maDH=?", linkedOrderId) == null) {
                linkedOrderId = null;
            }
            insertTransaction(entityManager, payload, linkedOrderId, processStatus, note);
            return new PaymentResult(true, false, orderId, null, null, note);
        });
    }

    private void insertTransaction(EntityManager entityManager, SePayWebhookPayload payload, Integer orderId,
                                   String processStatus, String note) {
        executeUpdate(entityManager, """
                INSERT INTO GIAO_DICH_SEPAY(
                    maGiaoDichSePay,maDH,gateway,transactionDate,accountNumber,subAccount,code,content,
                    transferType,description,transferAmount,accumulated,referenceCode,trangThaiXuLy,ghiChu,rawPayload)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, payload.getId(), orderId, trimTo(payload.getGateway(), 80), trimTo(payload.getTransactionDate(), 40),
                trimTo(payload.getAccountNumber(), 50), trimTo(payload.getSubAccount(), 100), trimTo(payload.getCode(), 100),
                trimTo(payload.getContent(), 500), trimTo(payload.getTransferType(), 10), trimTo(payload.getDescription(), 500),
                money(payload.getTransferAmount()), money(payload.getAccumulated()), trimTo(payload.getReferenceCode(), 120),
                processStatus, note, GSON.toJson(payload));
    }

    private Integer extractOrderId(SePayWebhookPayload payload, String prefix) {
        String normalizedPrefix = clean(prefix).isBlank() ? "DH" : clean(prefix).toUpperCase(Locale.ROOT);
        String text = clean(payload.getCode()) + " " + clean(payload.getContent());
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?i)(?:^|[^A-Z0-9])" + java.util.regex.Pattern.quote(normalizedPrefix) + "\\s*0*(\\d{1,10})(?:[^0-9]|$)")
                .matcher(" " + text + " ");
        if (!matcher.find()) return null;
        try {
            long value = Long.parseLong(matcher.group(1));
            return value > 0 && value <= Integer.MAX_VALUE ? (int) value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private BigDecimal money(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        try { return new BigDecimal(String.valueOf(value)); }
        catch (Exception ignored) { return BigDecimal.ZERO; }
    }

    private String normalizeAccount(String value) {
        return clean(value).replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimTo(String value, int maxLength) {
        String text = clean(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
