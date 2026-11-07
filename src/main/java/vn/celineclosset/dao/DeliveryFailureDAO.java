package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Quy trình chứng minh giao không thành công: tối thiểu ba ngày khác nhau và
 * tổng ít nhất ba lần liên hệ, sau đó ADMIN duyệt mới được hủy đơn.
 */
public class DeliveryFailureDAO extends CrudDAO {
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean schemaReady;

    public void ensureSchema() throws SQLException {
        if (schemaReady) return;
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) return;
            executeUpdate("""
                    IF COL_LENGTH('dbo.DON_HANG','hangDaHoanKho') IS NULL
                        ALTER TABLE dbo.DON_HANG ADD hangDaHoanKho BIT NOT NULL CONSTRAINT DF_DON_HANG_HANG_DA_HOAN_KHO DEFAULT 0;
                    """);
            executeUpdate("""
                    IF OBJECT_ID(N'dbo.YEU_CAU_GIAO_THAT_BAI', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.YEU_CAU_GIAO_THAT_BAI (
                            maYCGTB INT IDENTITY(1,1) PRIMARY KEY,
                            maDH INT NOT NULL UNIQUE,
                            maNhanVien INT NOT NULL,
                            trangThai VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
                            lyDo NVARCHAR(500) NULL,
                            ghiChuAdmin NVARCHAR(700) NULL,
                            maAdminDuyet INT NULL,
                            ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            ngayGuiDuyet DATETIME2 NULL,
                            ngayDuyet DATETIME2 NULL,
                            CONSTRAINT FK_YCGTB_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
                            CONSTRAINT FK_YCGTB_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
                            CONSTRAINT FK_YCGTB_ADMIN FOREIGN KEY (maAdminDuyet) REFERENCES dbo.TAI_KHOAN(maTK),
                            CONSTRAINT CK_YCGTB_TRANG_THAI CHECK (trangThai IN ('DRAFT','PENDING','APPROVED','REJECTED'))
                        );
                        CREATE INDEX IX_YCGTB_TRANG_THAI ON dbo.YEU_CAU_GIAO_THAT_BAI(trangThai,ngayGuiDuyet DESC);
                    END
                    """);
            executeUpdate("""
                    IF OBJECT_ID(N'dbo.LAN_GIAO_THAT_BAI', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.LAN_GIAO_THAT_BAI (
                            maLan INT IDENTITY(1,1) PRIMARY KEY,
                            maYCGTB INT NOT NULL,
                            ngayGiao DATE NOT NULL,
                            soLanGoi INT NOT NULL DEFAULT 1,
                            hinhAnh VARCHAR(500) NOT NULL,
                            ghiChu NVARCHAR(700) NULL,
                            ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            CONSTRAINT FK_LGTB_YEU_CAU FOREIGN KEY (maYCGTB) REFERENCES dbo.YEU_CAU_GIAO_THAT_BAI(maYCGTB),
                            CONSTRAINT CK_LGTB_SO_LAN_GOI CHECK (soLanGoi >= 1),
                            CONSTRAINT UQ_LGTB_NGAY UNIQUE (maYCGTB,ngayGiao)
                        );
                        CREATE INDEX IX_LGTB_YEU_CAU ON dbo.LAN_GIAO_THAT_BAI(maYCGTB,ngayGiao);
                    END
                    """);
            new NotificationDAO().ensureSchema();
            schemaReady = true;
        }
    }

    public List<Map<String, Object>> requests(String role, int accountId) throws SQLException {
        ensureSchema();
        StringBuilder sql = new StringBuilder(summaryBase()).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if ("DELIVERY".equals(role)) {
            sql.append("AND yc.maNhanVien=? ");
            params.add(accountId);
        } else {
            sql.append("AND yc.trangThai IN ('PENDING','APPROVED','REJECTED') ");
        }
        sql.append(summaryGroupBy()).append(" ORDER BY CASE yc.trangThai WHEN 'PENDING' THEN 0 WHEN 'DRAFT' THEN 1 WHEN 'REJECTED' THEN 2 ELSE 3 END, yc.maYCGTB DESC");
        return query(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> eligibleOrders(int deliveryId) throws SQLException {
        ensureSchema();
        return query("""
                SELECT dh.maDH,dh.hoTenNhan,dh.soDienThoaiNhan,dh.diaChiNhan,dh.trangThai,
                       yc.maYCGTB,yc.trangThai AS trangThaiYeuCau,
                       COUNT(l.maLan) AS soNgayDaGiao,COALESCE(SUM(l.soLanGoi),0) AS tongLanGoi
                FROM DON_HANG dh
                LEFT JOIN YEU_CAU_GIAO_THAT_BAI yc ON yc.maDH=dh.maDH
                LEFT JOIN LAN_GIAO_THAT_BAI l ON l.maYCGTB=yc.maYCGTB
                WHERE dh.maNhanVien=?
                  AND dh.trangThai=N'Đang giao'
                GROUP BY dh.maDH,dh.hoTenNhan,dh.soDienThoaiNhan,dh.diaChiNhan,dh.trangThai,yc.maYCGTB,yc.trangThai
                ORDER BY CASE dh.trangThai WHEN N'Đang giao' THEN 0 ELSE 1 END,dh.maDH DESC
                """, deliveryId);
    }

    public Map<String, Object> requestById(int requestId, String role, int accountId) throws SQLException {
        ensureSchema();
        String sql = summaryBase() + " WHERE yc.maYCGTB=? " + ("DELIVERY".equals(role) ? "AND yc.maNhanVien=? " : "") + summaryGroupBy();
        return "DELIVERY".equals(role) ? queryOne(sql, requestId, accountId) : queryOne(sql, requestId);
    }

    public Map<String, Object> requestByOrder(int orderId, String role, int accountId) throws SQLException {
        ensureSchema();
        String sql = summaryBase() + " WHERE yc.maDH=? " + ("DELIVERY".equals(role) ? "AND yc.maNhanVien=? " : "") + summaryGroupBy();
        return "DELIVERY".equals(role) ? queryOne(sql, orderId, accountId) : queryOne(sql, orderId);
    }

    public List<Map<String, Object>> attempts(int requestId) throws SQLException {
        ensureSchema();
        return query("SELECT * FROM LAN_GIAO_THAT_BAI WHERE maYCGTB=? ORDER BY ngayGiao,maLan", requestId);
    }

    public int addAttempt(int orderId, int deliveryId, LocalDate deliveryDate, int callCount,
                          String imagePath, String note) throws SQLException {
        ensureSchema();
        if (deliveryDate == null || deliveryDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException("Ngày giao phải là hôm nay hoặc một ngày đã qua.");
        }
        if (callCount < 1 || callCount > 20) {
            throw new IllegalStateException("Số lần gọi trong ngày phải từ 1 đến 20.");
        }
        if (imagePath == null || imagePath.isBlank()) {
            throw new IllegalStateException("Mỗi lần giao phải có ảnh chụp lịch sử cuộc gọi.");
        }
        return inTransaction(entityManager -> {
            Map<String, Object> order = queryOne(entityManager, """
                    SELECT dh.maDH,dh.trangThai,dh.maNhanVien
                    FROM DON_HANG dh WITH (UPDLOCK,ROWLOCK)
                    WHERE dh.maDH=? AND dh.maNhanVien=? AND dh.trangThai=N'Đang giao'
                    """, orderId, deliveryId);
            if (order == null) throw new IllegalStateException("Đơn không thuộc shipper hoặc chưa ở trạng thái Đang giao.");

            Map<String, Object> request = queryOne(entityManager,
                    "SELECT maYCGTB,trangThai FROM YEU_CAU_GIAO_THAT_BAI WITH (UPDLOCK,ROWLOCK) WHERE maDH=?", orderId);
            int requestId;
            if (request == null) {
                Map<String, Object> created = queryOne(entityManager, """
                        INSERT INTO YEU_CAU_GIAO_THAT_BAI(maDH,maNhanVien,trangThai)
                        OUTPUT INSERTED.maYCGTB AS maYCGTB VALUES(?,?,'DRAFT')
                        """, orderId, deliveryId);
                requestId = ((Number) created.get("maYCGTB")).intValue();
            } else {
                String state = String.valueOf(request.get("trangThai"));
                if (!("DRAFT".equals(state) || "REJECTED".equals(state))) {
                    throw new IllegalStateException("Yêu cầu đã gửi duyệt nên không thể thêm minh chứng.");
                }
                requestId = ((Number) request.get("maYCGTB")).intValue();
                if ("REJECTED".equals(state)) {
                    executeUpdate(entityManager, "UPDATE YEU_CAU_GIAO_THAT_BAI SET trangThai='DRAFT',ghiChuAdmin=NULL,maAdminDuyet=NULL,ngayGuiDuyet=NULL,ngayDuyet=NULL WHERE maYCGTB=?", requestId);
                }
            }

            Map<String,Object> sameDay = queryOne(entityManager,
                    "SELECT maLan FROM LAN_GIAO_THAT_BAI WHERE maYCGTB=? AND ngayGiao=?", requestId, deliveryDate);
            if (sameDay != null) {
                throw new IllegalStateException("Ngày này đã có minh chứng. Mỗi ngày chỉ được ghi nhận một lần giao.");
            }
            executeUpdate(entityManager, """
                    INSERT INTO LAN_GIAO_THAT_BAI(maYCGTB,ngayGiao,soLanGoi,hinhAnh,ghiChu)
                    VALUES(?,?,?,?,?)
                    """, requestId, deliveryDate, callCount, imagePath, clean(note));
            log(entityManager, deliveryId, orderId, "Ghi nhận giao không thành công",
                    "Ngày " + deliveryDate + ", đã gọi " + callCount + " lần. " + clean(note));
            return requestId;
        });
    }

    public void submit(int orderId, int deliveryId, String reason) throws SQLException {
        ensureSchema();
        inTransaction(entityManager -> {
            Map<String, Object> request = queryOne(entityManager, """
                    SELECT yc.maYCGTB,yc.trangThai,COUNT(l.maLan) AS soNgay,COALESCE(SUM(l.soLanGoi),0) AS tongLanGoi
                    FROM YEU_CAU_GIAO_THAT_BAI yc WITH (UPDLOCK,ROWLOCK)
                    LEFT JOIN LAN_GIAO_THAT_BAI l ON l.maYCGTB=yc.maYCGTB
                    JOIN DON_HANG dh ON dh.maDH=yc.maDH
                    WHERE yc.maDH=? AND yc.maNhanVien=? AND dh.maNhanVien=? AND dh.trangThai=N'Đang giao'
                    GROUP BY yc.maYCGTB,yc.trangThai
                    """, orderId, deliveryId, deliveryId);
            if (request == null) throw new IllegalStateException("Chưa có minh chứng giao hàng cho đơn này.");
            String state = String.valueOf(request.get("trangThai"));
            if (!("DRAFT".equals(state) || "REJECTED".equals(state))) {
                throw new IllegalStateException("Yêu cầu này đã được gửi duyệt.");
            }
            int days = ((Number) request.get("soNgay")).intValue();
            int calls = ((Number) request.get("tongLanGoi")).intValue();
            if (days < 3 || calls < 3) {
                throw new IllegalStateException("Cần đủ 3 ngày giao khác nhau và tổng ít nhất 3 lần gọi khách.");
            }
            int requestId = ((Number) request.get("maYCGTB")).intValue();
            executeUpdate(entityManager, """
                    UPDATE YEU_CAU_GIAO_THAT_BAI
                    SET trangThai='PENDING',lyDo=?,ngayGuiDuyet=SYSDATETIME(),ghiChuAdmin=NULL,maAdminDuyet=NULL,ngayDuyet=NULL
                    WHERE maYCGTB=?
                    """, clean(reason), requestId);
            log(entityManager, deliveryId, orderId, "Gửi duyệt giao thất bại",
                    "Đã gửi ADMIN duyệt sau " + days + " ngày giao và " + calls + " lần gọi khách.");
            return null;
        });
    }

    public void review(int requestId, int adminId, boolean approve, String adminNote) throws SQLException {
        ensureSchema();
        inTransaction(entityManager -> {
            Map<String, Object> request = queryOne(entityManager, """
                    SELECT yc.*,dh.maTK,dh.maVoucher,dh.trangThai AS trangThaiDon,dh.hangDaHoanKho,
                           COUNT(l.maLan) AS soNgay,COALESCE(SUM(l.soLanGoi),0) AS tongLanGoi
                    FROM YEU_CAU_GIAO_THAT_BAI yc WITH (UPDLOCK,ROWLOCK)
                    JOIN DON_HANG dh WITH (UPDLOCK,ROWLOCK) ON dh.maDH=yc.maDH
                    LEFT JOIN LAN_GIAO_THAT_BAI l ON l.maYCGTB=yc.maYCGTB
                    WHERE yc.maYCGTB=? AND yc.trangThai='PENDING'
                    GROUP BY yc.maYCGTB,yc.maDH,yc.maNhanVien,yc.trangThai,yc.lyDo,yc.ghiChuAdmin,yc.maAdminDuyet,
                             yc.ngayTao,yc.ngayGuiDuyet,yc.ngayDuyet,dh.maTK,dh.maVoucher,dh.trangThai,dh.hangDaHoanKho
                    """, requestId);
            if (request == null) throw new IllegalStateException("Yêu cầu không còn ở trạng thái chờ duyệt.");
            int orderId = ((Number) request.get("maDH")).intValue();
            int customerId = ((Number) request.get("maTK")).intValue();
            int deliveryId = ((Number) request.get("maNhanVien")).intValue();
            int days = ((Number) request.get("soNgay")).intValue();
            int calls = ((Number) request.get("tongLanGoi")).intValue();

            if (!approve) {
                executeUpdate(entityManager, """
                        UPDATE YEU_CAU_GIAO_THAT_BAI SET trangThai='REJECTED',ghiChuAdmin=?,maAdminDuyet=?,ngayDuyet=SYSDATETIME()
                        WHERE maYCGTB=?
                        """, clean(adminNote), adminId, requestId);
                log(entityManager, adminId, orderId, "Từ chối giao thất bại", clean(adminNote));
                log(entityManager, deliveryId, orderId, "Yêu cầu bị từ chối", clean(adminNote));
                return null;
            }
            if (days < 3 || calls < 3) throw new IllegalStateException("Minh chứng chưa đủ 3 ngày và 3 lần gọi.");

            executeUpdate(entityManager, """
                    UPDATE YEU_CAU_GIAO_THAT_BAI SET trangThai='APPROVED',ghiChuAdmin=?,maAdminDuyet=?,ngayDuyet=SYSDATETIME()
                    WHERE maYCGTB=?
                    """, clean(adminNote), adminId, requestId);

            int changed = executeUpdate(entityManager, """
                    UPDATE DON_HANG
                    SET trangThai=N'Đã hủy',lyDoHuy=N'Không giao được hàng sau 3 ngày và ít nhất 3 lần liên hệ khách',
                        nguoiHuy='ADMIN',ngayHuy=SYSDATETIME(),
                        ghiChu=CONCAT(ISNULL(ghiChu,N''),CASE WHEN ISNULL(ghiChu,N'')='' THEN N'' ELSE N' | ' END,
                            N'ADMIN duyệt không giao được hàng: ',?),
                        hangDaHoanKho=1
                    WHERE maDH=? AND trangThai NOT IN (N'Đã hủy',N'Hoàn thành')
                    """, clean(adminNote), orderId);
            if (changed == 0) throw new IllegalStateException("Đơn đã kết thúc nên không thể duyệt hủy.");

            Object restored = request.get("hangDaHoanKho");
            boolean wasRestored = restored instanceof Number number && number.intValue() == 1;
            if (!wasRestored) {
                executeUpdate(entityManager, """
                        UPDATE sp SET sp.soLuongTon=sp.soLuongTon+ct.soLuong
                        FROM SAN_PHAM sp JOIN CHI_TIET_DON_HANG ct ON ct.maSP=sp.maSP WHERE ct.maDH=?
                        """, orderId);
                restoreVoucher(entityManager, request, orderId, customerId);
            }

            executeUpdate(entityManager, """
                    UPDATE THANH_TOAN
                    SET trangThai=CASE WHEN trangThai='PAID' THEN trangThai ELSE 'CANCELLED' END,
                        trangThaiDoiSoat=CASE WHEN trangThai='PAID' THEN 'REVIEW' ELSE trangThaiDoiSoat END,
                        ghiChuDoiSoat=CASE WHEN trangThai='PAID' THEN N'Đơn không giao được; cần xử lý hoàn tiền' ELSE N'Đơn bị hủy do giao không thành công' END,
                        ngayCapNhat=SYSDATETIME()
                    WHERE maDH=?
                    """, orderId);

            executeUpdate(entityManager, """
                    INSERT INTO THONG_BAO_TAI_KHOAN(maTK,tieuDe,noiDung,duongDan,loai)
                    VALUES(?,N'Không giao được đơn hàng',?,?,'DELIVERY_FAILED')
                    """, customerId,
                    "Đơn #" + orderId + " đã được xác nhận không giao được sau 3 ngày và nhiều lần liên hệ. Đơn đã được hủy.",
                    "/orders?id=" + orderId);
            log(entityManager, adminId, orderId, "Duyệt giao thất bại",
                    "Đã duyệt hủy đơn sau " + days + " ngày và " + calls + " lần liên hệ.");
            log(entityManager, deliveryId, orderId, "Đơn không giao được",
                    "ADMIN đã duyệt hồ sơ minh chứng và hủy đơn.");
            return null;
        });
    }

    private void restoreVoucher(EntityManager entityManager, Map<String, Object> request, int orderId, int customerId) {
        Object voucherValue = request.get("maVoucher");
        if (!(voucherValue instanceof Number voucherNumber)) return;
        int voucherId = voucherNumber.intValue();
        executeUpdate(entityManager,
                "UPDATE VOUCHER SET daDung=CASE WHEN daDung>0 THEN daDung-1 ELSE 0 END WHERE maVoucher=?", voucherId);
        executeUpdate(entityManager, """
                UPDATE KHACH_HANG_VOUCHER SET trangThai='AVAILABLE',ngaySuDung=NULL,maDH=NULL
                WHERE maTK=? AND maVoucher=? AND maDH=? AND trangThai='USED'
                """, customerId, voucherId, orderId);
    }

    private String summaryBase() {
        return """
                SELECT yc.*,dh.trangThai AS trangThaiDon,dh.hoTenNhan,dh.soDienThoaiNhan,dh.diaChiNhan,
                       dh.maTK,tk.email,nv.hoTen AS tenNhanVien,ad.hoTen AS tenAdminDuyet,
                       COUNT(l.maLan) AS soNgayDaGiao,COALESCE(SUM(l.soLanGoi),0) AS tongLanGoi
                FROM YEU_CAU_GIAO_THAT_BAI yc
                JOIN DON_HANG dh ON dh.maDH=yc.maDH
                JOIN TAI_KHOAN tk ON tk.maTK=dh.maTK
                JOIN TAI_KHOAN nv ON nv.maTK=yc.maNhanVien
                LEFT JOIN TAI_KHOAN ad ON ad.maTK=yc.maAdminDuyet
                LEFT JOIN LAN_GIAO_THAT_BAI l ON l.maYCGTB=yc.maYCGTB
                """;
    }

    private String summaryGroupBy() {
        return " GROUP BY yc.maYCGTB,yc.maDH,yc.maNhanVien,yc.trangThai,yc.lyDo,yc.ghiChuAdmin,yc.maAdminDuyet,"
                + "yc.ngayTao,yc.ngayGuiDuyet,yc.ngayDuyet,dh.trangThai,dh.hoTenNhan,dh.soDienThoaiNhan,dh.diaChiNhan,"
                + "dh.maTK,tk.email,nv.hoTen,ad.hoTen ";
    }

    private void log(EntityManager entityManager, int staffId, int orderId, String action, String content) {
        executeUpdate(entityManager, """
                INSERT INTO NHAT_KY_NHAN_VIEN(maNhanVien,maDH,hanhDong,noiDung) VALUES(?,?,?,?)
                """, staffId, orderId, action, content);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? "Không có ghi chú" : value.trim();
    }
}
