package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;
import vn.celineclosset.util.AppConfig;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Quy trình trả hàng sau khi đơn hoàn tất.
 *
 * Khách được tạo yêu cầu trong 7 ngày, cung cấp tài khoản ngân hàng và chỉ được
 * sửa thông tin nhận tiền trong 2 ngày đầu. DELIVERY cập nhật hai chặng đầu,
 * ADMIN xác nhận hàng hoàn, bắt đầu hoàn tiền và xác nhận đã trả tiền.
 */
public class ReturnRequestDAO extends CrudDAO {
    public static final String REQUESTED = "REQUESTED";
    public static final String SHIPPER_RECEIVED = "SHIPPER_RECEIVED";
    public static final String AT_POST_OFFICE = "AT_POST_OFFICE";
    public static final String RETURN_COMPLETED = "RETURN_COMPLETED";
    public static final String REFUND_PROCESSING = "REFUND_PROCESSING";
    public static final String REFUNDED = "REFUNDED";
    public static final String REJECTED = "REJECTED";

    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean schemaReady;

    public void ensureSchema() throws SQLException {
        if (schemaReady) return;
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) return;
            executeUpdate("""
                    IF COL_LENGTH(N'dbo.DON_HANG', N'ngayHoanThanh') IS NULL
                        ALTER TABLE dbo.DON_HANG ADD ngayHoanThanh DATETIME2 NULL;

                    UPDATE dbo.DON_HANG
                    SET ngayHoanThanh=COALESCE(ngayHoanThanh,ngayDat)
                    WHERE trangThai=N'Hoàn thành' AND ngayHoanThanh IS NULL;

                    IF OBJECT_ID(N'dbo.YEU_CAU_TRA_HANG', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.YEU_CAU_TRA_HANG (
                            maYCTH INT IDENTITY(1,1) PRIMARY KEY,
                            maDH INT NOT NULL UNIQUE,
                            maTK INT NOT NULL,
                            maNhanVien INT NULL,
                            maAdminXuLy INT NULL,
                            lyDo NVARCHAR(1000) NOT NULL,
                            nganHang NVARCHAR(120) NOT NULL,
                            soTaiKhoan VARCHAR(50) NOT NULL,
                            chuTaiKhoan NVARCHAR(120) NOT NULL,
                            soTienHoan DECIMAL(12,2) NOT NULL,
                            trangThai VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
                            ghiChuXuLy NVARCHAR(1000) NULL,
                            ngayYeuCau DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            ngaySuaNganHang DATETIME2 NULL,
                            ngayShipperNhan DATETIME2 NULL,
                            ngayDenBuuDien DATETIME2 NULL,
                            ngayHoanHang DATETIME2 NULL,
                            ngayBatDauHoanTien DATETIME2 NULL,
                            ngayHoanTien DATETIME2 NULL,
                            ngayCapNhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            CONSTRAINT FK_YCTH_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
                            CONSTRAINT FK_YCTH_KHACH_HANG FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
                            CONSTRAINT FK_YCTH_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
                            CONSTRAINT FK_YCTH_ADMIN FOREIGN KEY (maAdminXuLy) REFERENCES dbo.TAI_KHOAN(maTK),
                            CONSTRAINT CK_YCTH_TRANG_THAI CHECK (trangThai IN
                                ('REQUESTED','SHIPPER_RECEIVED','AT_POST_OFFICE','RETURN_COMPLETED','REFUND_PROCESSING','REFUNDED','REJECTED')),
                            CONSTRAINT CK_YCTH_SO_TIEN CHECK (soTienHoan >= 0)
                        );
                        CREATE INDEX IX_YCTH_TRANG_THAI ON dbo.YEU_CAU_TRA_HANG(trangThai,ngayCapNhat DESC);
                        CREATE INDEX IX_YCTH_NHAN_VIEN ON dbo.YEU_CAU_TRA_HANG(maNhanVien,trangThai,ngayCapNhat DESC);
                    END;

                    IF OBJECT_ID(N'dbo.HINH_ANH_TRA_HANG', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.HINH_ANH_TRA_HANG (
                            maAnhTra INT IDENTITY(1,1) PRIMARY KEY,
                            maYCTH INT NOT NULL,
                            duongDan VARCHAR(500) NOT NULL,
                            ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            CONSTRAINT FK_HATH_YEU_CAU FOREIGN KEY (maYCTH)
                                REFERENCES dbo.YEU_CAU_TRA_HANG(maYCTH) ON DELETE CASCADE
                        );
                        CREATE INDEX IX_HATH_YEU_CAU ON dbo.HINH_ANH_TRA_HANG(maYCTH,maAnhTra);
                    END;
                    """);
            schemaReady = true;
        }
    }

    public Map<String, Object> returnForCustomer(int orderId, int customerId) throws SQLException {
        ensureSchema();
        return queryOne(summarySelect() + " WHERE yc.maDH=? AND yc.maTK=?", orderId, customerId);
    }

    public boolean canRequest(int orderId, int customerId) throws SQLException {
        ensureSchema();
        int days = returnDays();
        return queryOne("""
                SELECT TOP 1 dh.maDH
                FROM DON_HANG dh
                JOIN THANH_TOAN tt ON tt.maDH=dh.maDH
                WHERE dh.maDH=? AND dh.maTK=? AND dh.trangThai=N'Hoàn thành'
                  AND tt.trangThai='PAID'
                  AND SYSDATETIME() <= DATEADD(DAY,?,COALESCE(dh.ngayHoanThanh,dh.ngayDat))
                  AND NOT EXISTS(SELECT 1 FROM YEU_CAU_TRA_HANG yc WHERE yc.maDH=dh.maDH)
                """, orderId, customerId, days) != null;
    }

    public int daysRemaining(int orderId, int customerId) throws SQLException {
        ensureSchema();
        Map<String, Object> row = queryOne("""
                SELECT CASE WHEN dh.trangThai<>N'Hoàn thành' THEN 0
                            WHEN COALESCE(tt.trangThai,'PENDING')<>'PAID' THEN 0
                            WHEN SYSDATETIME()>DATEADD(DAY,?,COALESCE(dh.ngayHoanThanh,dh.ngayDat)) THEN 0
                            ELSE DATEDIFF(DAY,SYSDATETIME(),DATEADD(DAY,?,COALESCE(dh.ngayHoanThanh,dh.ngayDat)))+1 END AS soNgay
                FROM DON_HANG dh LEFT JOIN THANH_TOAN tt ON tt.maDH=dh.maDH
                WHERE dh.maDH=? AND dh.maTK=?
                """, returnDays(), returnDays(), orderId, customerId);
        return row == null ? 0 : Math.max(0, ((Number) row.get("soNgay")).intValue());
    }

    public void createRequest(int orderId, int customerId, String reason,
                              String bankName, String accountNumber, String ownerName,
                              List<String> images) throws SQLException {
        ensureSchema();
        String cleanReason = requireText(reason, "Vui lòng nhập lý do trả hàng.", 1000);
        String cleanBank = requireText(bankName, "Vui lòng nhập tên ngân hàng.", 120);
        String cleanAccount = requireAccount(accountNumber);
        String cleanOwner = requireText(ownerName, "Vui lòng nhập tên chủ tài khoản.", 120).toUpperCase();
        List<String> safeImages = images == null ? Collections.emptyList()
                : images.stream().filter(path -> path != null && !path.isBlank()).limit(3).toList();

        inTransaction(entityManager -> {
            Map<String, Object> order = queryOne(entityManager, """
                    SELECT dh.maDH,dh.maTK,dh.trangThai,dh.tongTien,dh.maNhanVien,
                           COALESCE(dh.ngayHoanThanh,dh.ngayDat) AS mocHoanThanh,
                           COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    LEFT JOIN THANH_TOAN tt WITH (UPDLOCK,ROWLOCK) ON tt.maDH=dh.maDH
                    WHERE dh.maDH=? AND dh.maTK=?
                    """, orderId, customerId);
            if (order == null) throw new IllegalStateException("Không tìm thấy đơn hàng của bạn.");
            if (!"Hoàn thành".equals(String.valueOf(order.get("trangThai")))) {
                throw new IllegalStateException("Chỉ đơn đã hoàn thành mới được yêu cầu trả hàng.");
            }
            if (!"PAID".equals(String.valueOf(order.get("trangThaiThanhToan")))) {
                throw new IllegalStateException("Đơn chưa thanh toán thành công nên chưa thể tạo yêu cầu trả hàng.");
            }
            Map<String, Object> window = queryOne(entityManager, """
                    SELECT CASE WHEN SYSDATETIME()<=DATEADD(DAY,?,COALESCE(ngayHoanThanh,ngayDat)) THEN 1 ELSE 0 END AS hopLe
                    FROM DON_HANG WHERE maDH=?
                    """, returnDays(), orderId);
            if (window == null || ((Number) window.get("hopLe")).intValue() != 1) {
                throw new IllegalStateException("Đơn đã quá thời hạn trả hàng " + returnDays() + " ngày.");
            }
            if (queryOne(entityManager, "SELECT maYCTH FROM YEU_CAU_TRA_HANG WHERE maDH=?", orderId) != null) {
                throw new IllegalStateException("Đơn này đã có yêu cầu trả hàng.");
            }

            Integer deliveryId = order.get("maNhanVien") instanceof Number number ? number.intValue() : null;
            Map<String, Object> created = queryOne(entityManager, """
                    INSERT INTO YEU_CAU_TRA_HANG
                        (maDH,maTK,maNhanVien,lyDo,nganHang,soTaiKhoan,chuTaiKhoan,soTienHoan,trangThai)
                    OUTPUT INSERTED.maYCTH AS maYCTH
                    VALUES(?,?,?,?,?,?,?,?,'REQUESTED')
                    """, orderId, customerId, deliveryId, cleanReason, cleanBank, cleanAccount, cleanOwner,
                    (BigDecimal) order.get("tongTien"));
            int requestId = ((Number) created.get("maYCTH")).intValue();
            for (String path : safeImages) {
                executeUpdate(entityManager,
                        "INSERT INTO HINH_ANH_TRA_HANG(maYCTH,duongDan) VALUES(?,?)", requestId, path);
            }
            notifyAdmins(entityManager, "Yêu cầu trả hàng mới",
                    "Khách đã tạo yêu cầu trả hàng cho đơn #" + orderId + ".", "/admin/returns?id=" + requestId,
                    "RETURN_REQUEST");
            if (deliveryId != null) {
                notifyAccount(entityManager, deliveryId, "Có đơn cần nhận hàng hoàn",
                        "Đơn #" + orderId + " có yêu cầu trả hàng. Hãy liên hệ khách để nhận hàng.",
                        "/admin/returns?id=" + requestId, "RETURN_PICKUP");
            }
            return null;
        });
    }

    public void updateBank(int requestId, int customerId, String bankName,
                           String accountNumber, String ownerName) throws SQLException {
        ensureSchema();
        String cleanBank = requireText(bankName, "Vui lòng nhập tên ngân hàng.", 120);
        String cleanAccount = requireAccount(accountNumber);
        String cleanOwner = requireText(ownerName, "Vui lòng nhập tên chủ tài khoản.", 120).toUpperCase();
        int updated = executeUpdate("""
                UPDATE YEU_CAU_TRA_HANG
                SET nganHang=?,soTaiKhoan=?,chuTaiKhoan=?,ngaySuaNganHang=SYSDATETIME(),ngayCapNhat=SYSDATETIME()
                WHERE maYCTH=? AND maTK=?
                  AND SYSDATETIME()<=DATEADD(DAY,?,ngayYeuCau)
                  AND trangThai NOT IN ('REFUND_PROCESSING','REFUNDED','REJECTED')
                """, cleanBank, cleanAccount, cleanOwner, requestId, customerId, editBankDays());
        if (updated == 0) {
            throw new IllegalStateException("Thông tin ngân hàng chỉ được sửa trong " + editBankDays()
                    + " ngày đầu và trước khi hoàn tiền được xử lý.");
        }
    }

    public List<Map<String, Object>> requests(String role, int accountId) throws SQLException {
        ensureSchema();
        String where = "ADMIN".equals(role) ? " WHERE 1=1 "
                : " WHERE COALESCE(yc.maNhanVien,dh.maNhanVien)=? ";
        return "ADMIN".equals(role)
                ? query(summarySelect() + where + " ORDER BY CASE yc.trangThai WHEN 'REQUESTED' THEN 0 WHEN 'SHIPPER_RECEIVED' THEN 1 WHEN 'AT_POST_OFFICE' THEN 2 WHEN 'RETURN_COMPLETED' THEN 3 WHEN 'REFUND_PROCESSING' THEN 4 ELSE 5 END,yc.ngayCapNhat DESC")
                : query(summarySelect() + where + " ORDER BY yc.ngayCapNhat DESC", accountId);
    }

    public Map<String, Object> requestById(int requestId, String role, int accountId) throws SQLException {
        ensureSchema();
        if ("ADMIN".equals(role)) {
            return queryOne(summarySelect() + " WHERE yc.maYCTH=?", requestId);
        }
        return queryOne(summarySelect() + " WHERE yc.maYCTH=? AND COALESCE(yc.maNhanVien,dh.maNhanVien)=?",
                requestId, accountId);
    }

    public List<Map<String, Object>> images(int requestId) throws SQLException {
        ensureSchema();
        return query("SELECT * FROM HINH_ANH_TRA_HANG WHERE maYCTH=? ORDER BY maAnhTra", requestId);
    }

    public List<Map<String, Object>> deliveryAccounts() throws SQLException {
        return query("SELECT maTK,hoTen,email FROM TAI_KHOAN WHERE vaiTro='DELIVERY' AND trangThai=1 ORDER BY hoTen");
    }

    public void assignDelivery(int requestId, int adminId, int deliveryId) throws SQLException {
        ensureSchema();
        inTransaction(entityManager -> {
            if (!isRole(entityManager, adminId, "ADMIN")) throw new IllegalAccessException("Chỉ ADMIN được phân công.");
            if (!isRole(entityManager, deliveryId, "DELIVERY")) throw new IllegalStateException("Shipper không hợp lệ.");
            Map<String, Object> request = queryOne(entityManager,
                    "SELECT maDH,trangThai FROM YEU_CAU_TRA_HANG WITH (UPDLOCK,ROWLOCK) WHERE maYCTH=?", requestId);
            if (request == null) throw new IllegalStateException("Không tìm thấy yêu cầu trả hàng.");
            String status = String.valueOf(request.get("trangThai"));
            if (!(REQUESTED.equals(status) || SHIPPER_RECEIVED.equals(status))) {
                throw new IllegalStateException("Không thể đổi shipper ở giai đoạn hiện tại.");
            }
            executeUpdate(entityManager,
                    "UPDATE YEU_CAU_TRA_HANG SET maNhanVien=?,maAdminXuLy=?,ngayCapNhat=SYSDATETIME() WHERE maYCTH=?",
                    deliveryId, adminId, requestId);
            notifyAccount(entityManager, deliveryId, "Bạn được phân công nhận hàng hoàn",
                    "Bạn được phân công xử lý hàng hoàn của đơn #" + request.get("maDH") + ".",
                    "/admin/returns?id=" + requestId, "RETURN_PICKUP");
            return null;
        });
    }

    public void updateStatus(int requestId, int actorId, String role, String action, String note) throws SQLException {
        ensureSchema();
        inTransaction(entityManager -> {
            Map<String, Object> request = queryOne(entityManager, """
                    SELECT yc.*,dh.maNhanVien AS shipperGoc,dh.hangDaHoanKho,dh.maTK AS khachHang
                    FROM YEU_CAU_TRA_HANG yc WITH (UPDLOCK,ROWLOCK)
                    JOIN DON_HANG dh WITH (UPDLOCK,ROWLOCK) ON dh.maDH=yc.maDH
                    WHERE yc.maYCTH=?
                    """, requestId);
            if (request == null) throw new IllegalStateException("Không tìm thấy yêu cầu trả hàng.");
            String current = String.valueOf(request.get("trangThai"));
            int customerId = ((Number) request.get("khachHang")).intValue();
            int orderId = ((Number) request.get("maDH")).intValue();
            Integer assigned = request.get("maNhanVien") instanceof Number n ? n.intValue() : null;

            if ("DELIVERY".equals(role)) {
                if (assigned == null || assigned != actorId) {
                    throw new IllegalStateException("Yêu cầu trả hàng này không thuộc shipper đang đăng nhập.");
                }
                if ("receive".equals(action) && REQUESTED.equals(current)) {
                    setStatus(entityManager, requestId, SHIPPER_RECEIVED, "ngayShipperNhan", actorId, note);
                    notifyAccount(entityManager, customerId, "Shipper đã nhận hàng trả",
                            "Shipper đã nhận hàng trả của đơn #" + orderId + ".",
                            "/orders?id=" + orderId, "RETURN_PROGRESS");
                    return null;
                }
                if ("postOffice".equals(action) && SHIPPER_RECEIVED.equals(current)) {
                    setStatus(entityManager, requestId, AT_POST_OFFICE, "ngayDenBuuDien", actorId, note);
                    notifyAccount(entityManager, customerId, "Hàng trả đã đến bưu điện",
                            "Hàng trả của đơn #" + orderId + " đã đến điểm xử lý/bưu điện.",
                            "/orders?id=" + orderId, "RETURN_PROGRESS");
                    return null;
                }
                throw new IllegalStateException("Shipper không thể cập nhật bước này.");
            }

            if (!"ADMIN".equals(role) || !isRole(entityManager, actorId, "ADMIN")) {
                throw new IllegalAccessException("Chỉ ADMIN hoặc shipper được cập nhật yêu cầu trả hàng.");
            }

            if ("reject".equals(action)) {
                if (!(REQUESTED.equals(current) || SHIPPER_RECEIVED.equals(current))) {
                    throw new IllegalStateException("Chỉ được từ chối trước khi hàng đến bưu điện.");
                }
                executeUpdate(entityManager, """
                        UPDATE YEU_CAU_TRA_HANG SET trangThai='REJECTED',maAdminXuLy=?,ghiChuXuLy=?,ngayCapNhat=SYSDATETIME()
                        WHERE maYCTH=?
                        """, actorId, cleanNote(note), requestId);
                notifyAccount(entityManager, customerId, "Yêu cầu trả hàng bị từ chối",
                        "Yêu cầu trả hàng của đơn #" + orderId + " đã bị từ chối. Lý do: " + cleanNote(note),
                        "/orders?id=" + orderId, "RETURN_REJECTED");
                return null;
            }

            if ("receive".equals(action) && REQUESTED.equals(current)) {
                setStatus(entityManager, requestId, SHIPPER_RECEIVED, "ngayShipperNhan", actorId, note);
                notifyAccount(entityManager, customerId, "Shipper đã nhận hàng trả",
                        "Hàng trả của đơn #" + orderId + " đã được nhân viên tiếp nhận.",
                        "/orders?id=" + orderId, "RETURN_PROGRESS");
                return null;
            }
            if ("postOffice".equals(action) && SHIPPER_RECEIVED.equals(current)) {
                setStatus(entityManager, requestId, AT_POST_OFFICE, "ngayDenBuuDien", actorId, note);
                notifyAccount(entityManager, customerId, "Hàng trả đã đến bưu điện",
                        "Hàng trả của đơn #" + orderId + " đã đến điểm xử lý/bưu điện.",
                        "/orders?id=" + orderId, "RETURN_PROGRESS");
                return null;
            }
            if ("completeReturn".equals(action) && AT_POST_OFFICE.equals(current)) {
                restoreStockOnce(entityManager, orderId, request.get("hangDaHoanKho"));
                executeUpdate(entityManager, """
                        UPDATE YEU_CAU_TRA_HANG SET trangThai='RETURN_COMPLETED',maAdminXuLy=?,ghiChuXuLy=?,
                            ngayHoanHang=SYSDATETIME(),ngayCapNhat=SYSDATETIME() WHERE maYCTH=?
                        """, actorId, cleanNote(note), requestId);
                notifyAccount(entityManager, customerId, "Hoàn hàng thành công",
                        "Cửa hàng đã nhận và kiểm tra hàng trả của đơn #" + orderId + ".",
                        "/orders?id=" + orderId, "RETURN_PROGRESS");
                return null;
            }
            if ("startRefund".equals(action) && RETURN_COMPLETED.equals(current)) {
                executeUpdate(entityManager, """
                        UPDATE YEU_CAU_TRA_HANG SET trangThai='REFUND_PROCESSING',maAdminXuLy=?,ghiChuXuLy=?,
                            ngayBatDauHoanTien=SYSDATETIME(),ngayCapNhat=SYSDATETIME() WHERE maYCTH=?
                        """, actorId, cleanNote(note), requestId);
                notifyAccount(entityManager, customerId, "Đang xử lý trả tiền",
                        "Cửa hàng đang xử lý trả tiền cho đơn #" + orderId
                                + ". Tiền thường về tài khoản ngân hàng trong 3–4 ngày làm việc.",
                        "/orders?id=" + orderId, "REFUND_PROCESSING");
                return null;
            }
            if ("finishRefund".equals(action) && REFUND_PROCESSING.equals(current)) {
                executeUpdate(entityManager, """
                        UPDATE YEU_CAU_TRA_HANG SET trangThai='REFUNDED',maAdminXuLy=?,ghiChuXuLy=?,
                            ngayHoanTien=SYSDATETIME(),ngayCapNhat=SYSDATETIME() WHERE maYCTH=?
                        """, actorId, cleanNote(note), requestId);
                executeUpdate(entityManager, """
                        UPDATE THANH_TOAN SET trangThaiDoiSoat='NONE',
                            ghiChuDoiSoat=CONCAT(ISNULL(ghiChuDoiSoat,N''),CASE WHEN ISNULL(ghiChuDoiSoat,N'')='' THEN N'' ELSE N' | ' END,
                                N'Đã hoàn tiền trả hàng vào ',?,N' - ',?),ngayCapNhat=SYSDATETIME()
                        WHERE maDH=?
                        """, request.get("nganHang"), maskAccount(String.valueOf(request.get("soTaiKhoan"))), orderId);
                notifyAccount(entityManager, customerId, "Đã trả tiền đơn hàng",
                        "Cửa hàng đã hoàn " + request.get("soTienHoan") + "đ cho đơn #" + orderId
                                + " vào tài khoản ngân hàng đã đăng ký.",
                        "/orders?id=" + orderId, "REFUNDED");
                return null;
            }
            throw new IllegalStateException("Trạng thái hiện tại không phù hợp với thao tác này.");
        });
    }

    private void setStatus(EntityManager entityManager, int requestId, String status,
                           String dateColumn, int actorId, String note) {
        String safeColumn = switch (dateColumn) {
            case "ngayShipperNhan" -> "ngayShipperNhan";
            case "ngayDenBuuDien" -> "ngayDenBuuDien";
            default -> throw new IllegalArgumentException("Cột thời gian không hợp lệ.");
        };
        executeUpdate(entityManager, "UPDATE YEU_CAU_TRA_HANG SET trangThai=?," + safeColumn
                + "=SYSDATETIME(),ghiChuXuLy=?,ngayCapNhat=SYSDATETIME() WHERE maYCTH=?",
                status, cleanNote(note), requestId);
        log(entityManager, actorId, requestId, status, cleanNote(note));
    }

    private void restoreStockOnce(EntityManager entityManager, int orderId, Object restoredValue) {
        boolean restored = restoredValue instanceof Number number && number.intValue() == 1;
        if (restored) return;
        executeUpdate(entityManager, """
                UPDATE sp SET sp.soLuongTon=sp.soLuongTon+ct.soLuong
                FROM SAN_PHAM sp JOIN CHI_TIET_DON_HANG ct ON ct.maSP=sp.maSP
                WHERE ct.maDH=?
                """, orderId);
        executeUpdate(entityManager, "UPDATE DON_HANG SET hangDaHoanKho=1,loaiSuCo='RETURN',ngaySuCo=SYSDATETIME() WHERE maDH=?", orderId);
    }

    private String summarySelect() {
        return """
                SELECT yc.*,dh.trangThai AS trangThaiDon,dh.hoTenNhan,dh.soDienThoaiNhan,dh.diaChiNhan,
                       dh.tongTien,dh.ngayHoanThanh,dh.ngayDat,dh.maNhanVien AS shipperGoc,
                       tk.hoTen AS tenKhachHang,tk.email,
                       nv.hoTen AS tenShipper,nv.email AS emailShipper,
                       ad.hoTen AS tenAdmin,
                       CASE WHEN SYSDATETIME()<=DATEADD(DAY,%d,yc.ngayYeuCau)
                                  AND yc.trangThai NOT IN ('REFUND_PROCESSING','REFUNDED','REJECTED') THEN 1 ELSE 0 END AS duocSuaNganHang,
                       CASE yc.trangThai
                           WHEN 'REQUESTED' THEN 0
                           WHEN 'SHIPPER_RECEIVED' THEN 1
                           WHEN 'AT_POST_OFFICE' THEN 2
                           WHEN 'RETURN_COMPLETED' THEN 3
                           WHEN 'REFUND_PROCESSING' THEN 4
                           WHEN 'REFUNDED' THEN 4
                           ELSE 0 END AS buocTraHang,
                       DATEADD(DAY,%d,yc.ngayBatDauHoanTien) AS duKienHoanTien
                FROM YEU_CAU_TRA_HANG yc
                JOIN DON_HANG dh ON dh.maDH=yc.maDH
                JOIN TAI_KHOAN tk ON tk.maTK=yc.maTK
                LEFT JOIN TAI_KHOAN nv ON nv.maTK=yc.maNhanVien
                LEFT JOIN TAI_KHOAN ad ON ad.maTK=yc.maAdminXuLy
                """.formatted(editBankDays(), refundProcessingDays());
    }

    private void notifyAdmins(EntityManager entityManager, String title, String content, String path, String type) {
        executeUpdate(entityManager, """
                INSERT INTO THONG_BAO_TAI_KHOAN(maTK,tieuDe,noiDung,duongDan,loai)
                SELECT maTK,?,?,?,? FROM TAI_KHOAN WHERE vaiTro='ADMIN' AND trangThai=1
                """, title, content, path, type);
    }

    private void notifyAccount(EntityManager entityManager, int accountId, String title,
                               String content, String path, String type) {
        executeUpdate(entityManager, """
                INSERT INTO THONG_BAO_TAI_KHOAN(maTK,tieuDe,noiDung,duongDan,loai)
                VALUES(?,?,?,?,?)
                """, accountId, title, content, path, type);
    }

    private void log(EntityManager entityManager, int actorId, int requestId, String action, String content) {
        Map<String, Object> request = queryOne(entityManager,
                "SELECT maDH FROM YEU_CAU_TRA_HANG WHERE maYCTH=?", requestId);
        if (request == null || !isBackOffice(entityManager, actorId)) return;
        executeUpdate(entityManager, """
                INSERT INTO NHAT_KY_NHAN_VIEN(maNhanVien,maDH,hanhDong,noiDung) VALUES(?,?,?,?)
                """, actorId, request.get("maDH"), "Trả hàng: " + action, content);
    }

    private boolean isRole(EntityManager entityManager, int accountId, String role) {
        return queryOne(entityManager,
                "SELECT maTK FROM TAI_KHOAN WHERE maTK=? AND vaiTro=? AND trangThai=1", accountId, role) != null;
    }

    private boolean isBackOffice(EntityManager entityManager, int accountId) {
        return queryOne(entityManager,
                "SELECT maTK FROM TAI_KHOAN WHERE maTK=? AND vaiTro IN ('ADMIN','STAFF','DELIVERY') AND trangThai=1",
                accountId) != null;
    }

    private String requireText(String value, String message, int maxLength) {
        String clean = value == null ? "" : value.trim();
        if (clean.isBlank()) throw new IllegalArgumentException(message);
        return clean.length() > maxLength ? clean.substring(0, maxLength) : clean;
    }

    private String requireAccount(String value) {
        String clean = value == null ? "" : value.replaceAll("[^0-9A-Za-z]", "").trim();
        if (clean.length() < 6 || clean.length() > 50) {
            throw new IllegalArgumentException("Số tài khoản phải có từ 6 đến 50 ký tự.");
        }
        return clean;
    }

    private String cleanNote(String value) {
        String clean = value == null || value.isBlank() ? "Đã cập nhật tiến trình trả hàng." : value.trim();
        return clean.length() > 1000 ? clean.substring(0, 1000) : clean;
    }

    private String maskAccount(String account) {
        if (account == null || account.length() <= 4) return "****";
        return "****" + account.substring(account.length() - 4);
    }

    private int returnDays() {
        return Math.max(1, AppConfig.getInt("return.requestDays", 7));
    }

    private int editBankDays() {
        return Math.max(1, AppConfig.getInt("return.bankEditDays", 2));
    }

    private int refundProcessingDays() {
        return Math.max(1, AppConfig.getInt("return.refundProcessingDays", 4));
    }
}
