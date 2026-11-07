package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** DAO hỗ trợ khách hàng, gồm tạo phiếu, phân công và phản hồi. */
public class SupportDAO extends CrudDAO {

    public int create(Integer customerId, String name, String email, String phone,
                      String subject, String content) throws SQLException {
        if (customerId == null) {
            executeUpdate("""
                    INSERT INTO YEU_CAU_HO_TRO(hoTen,email,soDienThoai,chuDe,noiDung,trangThai)
                    VALUES(?,?,?,?,?,'MOI')
                    """, text(name), text(email).toLowerCase(), text(phone), text(subject), text(content));
        } else {
            executeUpdate("""
                    INSERT INTO YEU_CAU_HO_TRO(maTK,hoTen,email,soDienThoai,chuDe,noiDung,trangThai)
                    VALUES(?,?,?,?,?,?,'MOI')
                    """, customerId, text(name), text(email).toLowerCase(), text(phone), text(subject), text(content));
        }
        Map<String,Object> newest;
        if (customerId == null) {
            newest = queryOne("SELECT TOP 1 maYC FROM YEU_CAU_HO_TRO WHERE email=? ORDER BY maYC DESC", text(email).toLowerCase());
        } else {
            newest = queryOne("SELECT TOP 1 maYC FROM YEU_CAU_HO_TRO WHERE maTK=? ORDER BY maYC DESC", customerId);
        }
        if (newest == null) throw new SQLException("Không tạo được cuộc trò chuyện hỗ trợ.");
        int requestId = ((Number)newest.get("maYC")).intValue();
        if (customerId != null) {
            executeUpdate("INSERT INTO TIN_NHAN_HO_TRO(maYC,maNguoiGui,vaiTroNguoiGui,noiDung) VALUES(?,?, 'CUSTOMER', ?)",
                    requestId, customerId, text(content));
        }
        return requestId;
    }

    public List<Map<String, Object>> customerRequests(int customerId) throws SQLException {
        return query("""
                SELECT yc.*, nv.hoTen AS tenNhanVien
                FROM YEU_CAU_HO_TRO yc
                LEFT JOIN TAI_KHOAN nv ON nv.maTK=yc.maNhanVien
                WHERE yc.maTK=?
                ORDER BY yc.maYC DESC
                """, customerId);
    }

    public List<Map<String, Object>> backOfficeRequests(String role, int accountId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT yc.*, nv.hoTen AS tenNhanVien,
                       (SELECT COUNT(*) FROM TIN_NHAN_HO_TRO tn
                        WHERE tn.maYC=yc.maYC AND tn.vaiTroNguoiGui='CUSTOMER' AND tn.daDoc=0) AS tinChuaDoc
                FROM YEU_CAU_HO_TRO yc
                LEFT JOIN TAI_KHOAN nv ON nv.maTK=yc.maNhanVien
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if ("STAFF".equals(role)) {
            sql.append("AND yc.maNhanVien=? ");
            params.add(accountId);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND yc.trangThai=? ");
            params.add(status);
        }
        sql.append("ORDER BY CASE yc.trangThai WHEN 'MOI' THEN 0 WHEN 'DANG_XU_LY' THEN 1 ELSE 2 END, yc.maYC DESC");
        return query(sql.toString(), params.toArray());
    }

    public Map<String, Object> requestForBackOffice(int requestId, String role, int accountId) throws SQLException {
        if ("STAFF".equals(role)) {
            return queryOne("""
                    SELECT yc.*, nv.hoTen AS tenNhanVien
                    FROM YEU_CAU_HO_TRO yc
                    LEFT JOIN TAI_KHOAN nv ON nv.maTK=yc.maNhanVien
                    WHERE yc.maYC=? AND yc.maNhanVien=?
                    """, requestId, accountId);
        }
        return queryOne("""
                SELECT yc.*, nv.hoTen AS tenNhanVien
                FROM YEU_CAU_HO_TRO yc
                LEFT JOIN TAI_KHOAN nv ON nv.maTK=yc.maNhanVien
                WHERE yc.maYC=?
                """, requestId);
    }

    /** Số tin khách mới để STAFF/ADMIN thấy thông báo ngay trên menu. */
    public int unreadForBackOffice(String role, int accountId) throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT tn.maYC) AS total
                FROM TIN_NHAN_HO_TRO tn
                JOIN YEU_CAU_HO_TRO yc ON yc.maYC=tn.maYC
                WHERE tn.vaiTroNguoiGui='CUSTOMER' AND tn.daDoc=0
                  AND yc.trangThai<>'DA_DONG'
                """ + ("STAFF".equals(role) ? " AND yc.maNhanVien=?" : "");
        Map<String,Object> row = "STAFF".equals(role) ? queryOne(sql, accountId) : queryOne(sql);
        return row == null ? 0 : ((Number) row.get("total")).intValue();
    }

    /** Đánh dấu các tin khách đã được nhân viên mở xem. */
    public void markCustomerMessagesRead(int requestId, String role, int accountId) throws SQLException {
        if ("STAFF".equals(role)) {
            executeUpdate("""
                    UPDATE tn SET tn.daDoc=1
                    FROM TIN_NHAN_HO_TRO tn
                    JOIN YEU_CAU_HO_TRO yc ON yc.maYC=tn.maYC
                    WHERE tn.maYC=? AND tn.vaiTroNguoiGui='CUSTOMER' AND yc.maNhanVien=?
                    """, requestId, accountId);
        } else if ("ADMIN".equals(role)) {
            executeUpdate("UPDATE TIN_NHAN_HO_TRO SET daDoc=1 WHERE maYC=? AND vaiTroNguoiGui='CUSTOMER'", requestId);
        }
    }

    public void assign(int requestId, int staffId) throws SQLException {
        inTransaction(entityManager -> {
            Map<String, Object> staff = queryOne(entityManager,
                    "SELECT maTK FROM TAI_KHOAN WHERE maTK=? AND vaiTro='STAFF' AND trangThai=1", staffId);
            if (staff == null) throw new SQLException("Nhân viên không hợp lệ.");

            int updated = executeUpdate(entityManager, """
                    UPDATE YEU_CAU_HO_TRO
                    SET maNhanVien=?,trangThai='DANG_XU_LY',ngayCapNhat=SYSDATETIME()
                    WHERE maYC=?
                    """, staffId, requestId);
            if (updated == 0) throw new SQLException("Không tìm thấy yêu cầu hỗ trợ.");
            executeUpdate(entityManager,
                    "UPDATE TIN_NHAN_HO_TRO SET daDoc=0 WHERE maYC=? AND vaiTroNguoiGui='CUSTOMER'", requestId);
            log(entityManager, staffId, null, "Nhận yêu cầu hỗ trợ",
                    "Được phân công xử lý phiếu hỗ trợ #" + requestId + ".");
            return null;
        });
    }

    public void reply(int requestId, int actorId, String role, String response, String status) throws SQLException {
        inTransaction(entityManager -> {
            if ("STAFF".equals(role)) {
                Map<String, Object> owned = queryOne(entityManager,
                        "SELECT maYC FROM YEU_CAU_HO_TRO WHERE maYC=? AND maNhanVien=?", requestId, actorId);
                if (owned == null) throw new SQLException("Phiếu hỗ trợ không thuộc nhân viên này.");
            }
            String safeStatus = validStatus(status) ? status : "DA_PHAN_HOI";
            int updated = executeUpdate(entityManager, """
                    UPDATE YEU_CAU_HO_TRO
                    SET phanHoi=?,trangThai=?,ngayCapNhat=SYSDATETIME()
                    WHERE maYC=?
                    """, text(response), safeStatus, requestId);
            if (updated == 0) throw new SQLException("Không tìm thấy yêu cầu hỗ trợ.");
            executeUpdate(entityManager, "INSERT INTO TIN_NHAN_HO_TRO(maYC,maNguoiGui,vaiTroNguoiGui,noiDung) VALUES(?,?,?,?)",
                    requestId, actorId, role, text(response));

            if ("STAFF".equals(role)) {
                log(entityManager, actorId, null, "Phản hồi khách hàng",
                        "Đã xử lý phiếu hỗ trợ #" + requestId + " với trạng thái " + safeStatus + ".");
            }
            return null;
        });
    }

    public Map<String,Object> customerRequest(int requestId, int customerId) throws SQLException {
        return queryOne("SELECT * FROM YEU_CAU_HO_TRO WHERE maYC=? AND maTK=?", requestId, customerId);
    }

    public List<Map<String,Object>> messages(int requestId) throws SQLException {
        return query("SELECT tn.*,tk.hoTen FROM TIN_NHAN_HO_TRO tn LEFT JOIN TAI_KHOAN tk ON tk.maTK=tn.maNguoiGui WHERE tn.maYC=? ORDER BY tn.ngayGui,tn.maTN", requestId);
    }

    public void customerMessage(int requestId, int customerId, String content) throws SQLException {
        if (customerRequest(requestId, customerId) == null) {
            throw new SQLException("Cuộc trò chuyện không thuộc khách hàng.");
        }
        executeUpdate("INSERT INTO TIN_NHAN_HO_TRO(maYC,maNguoiGui,vaiTroNguoiGui,noiDung) VALUES(?,?,'CUSTOMER',?)",
                requestId, customerId, text(content));
        executeUpdate("UPDATE YEU_CAU_HO_TRO SET ngayCapNhat=SYSDATETIME() WHERE maYC=?", requestId);
    }

    public void insertBotMessage(int requestId, String content) throws SQLException {
        executeUpdate("INSERT INTO TIN_NHAN_HO_TRO(maYC,maNguoiGui,vaiTroNguoiGui,noiDung) VALUES(?,NULL,'BOT',?)",
                requestId, text(content));
        executeUpdate("UPDATE YEU_CAU_HO_TRO SET ngayCapNhat=SYSDATETIME() WHERE maYC=?", requestId);
    }

    public void touchRequest(int requestId, String status) throws SQLException {
        String safeStatus = validStatus(status) ? status : "MOI";
        executeUpdate("UPDATE YEU_CAU_HO_TRO SET trangThai=?,ngayCapNhat=SYSDATETIME() WHERE maYC=?",
                safeStatus, requestId);
    }

    public String assignedStaffName(int requestId) throws SQLException {
        Map<String, Object> row = queryOne("""
                SELECT tk.hoTen
                FROM YEU_CAU_HO_TRO yc
                JOIN TAI_KHOAN tk ON tk.maTK=yc.maNhanVien
                WHERE yc.maYC=?
                """, requestId);
        return row == null ? "" : String.valueOf(row.getOrDefault("hoTen", ""));
    }

    /**
     * Phân công công bằng: chọn STAFF đang hoạt động có ít phiếu chưa đóng nhất;
     * nếu bằng nhau thì chọn người lâu chưa nhận phiếu hơn.
     */
    public Map<String, Object> assignAvailableStaff(int requestId) throws SQLException {
        return inTransaction(entityManager -> {
            Map<String, Object> existing = queryOne(entityManager, """
                    SELECT tk.maTK,tk.hoTen
                    FROM YEU_CAU_HO_TRO yc
                    JOIN TAI_KHOAN tk ON tk.maTK=yc.maNhanVien
                    WHERE yc.maYC=? AND tk.vaiTro='STAFF' AND tk.trangThai=1
                    """, requestId);
            if (existing != null) return existing;

            Map<String, Object> staff = queryOne(entityManager, """
                    SELECT TOP 1 tk.maTK,tk.hoTen,COUNT(yc.maYC) AS soPhieuDangMo
                    FROM TAI_KHOAN tk
                    LEFT JOIN YEU_CAU_HO_TRO yc
                      ON yc.maNhanVien=tk.maTK
                     AND yc.trangThai IN ('MOI','DANG_XU_LY','DA_PHAN_HOI')
                    WHERE tk.vaiTro='STAFF' AND tk.trangThai=1
                    GROUP BY tk.maTK,tk.hoTen
                    ORDER BY COUNT(yc.maYC) ASC,
                             ISNULL(MAX(yc.ngayCapNhat),CONVERT(DATETIME2,'1900-01-01')) ASC,
                             tk.maTK ASC
                    """);
            if (staff == null) return null;

            int staffId = ((Number) staff.get("maTK")).intValue();
            int updated = executeUpdate(entityManager, """
                    UPDATE YEU_CAU_HO_TRO
                    SET maNhanVien=?,trangThai='DANG_XU_LY',ngayCapNhat=SYSDATETIME()
                    WHERE maYC=? AND maNhanVien IS NULL
                    """, staffId, requestId);
            if (updated == 0) {
                return queryOne(entityManager, """
                        SELECT tk.maTK,tk.hoTen
                        FROM YEU_CAU_HO_TRO yc
                        JOIN TAI_KHOAN tk ON tk.maTK=yc.maNhanVien
                        WHERE yc.maYC=?
                        """, requestId);
            }
            executeUpdate(entityManager,
                    "UPDATE TIN_NHAN_HO_TRO SET daDoc=0 WHERE maYC=? AND vaiTroNguoiGui='CUSTOMER'", requestId);
            log(entityManager, staffId, null, "Nhận yêu cầu hỗ trợ",
                    "Hệ thống tự động phân công phiếu hỗ trợ #" + requestId + ".");
            return staff;
        });
    }

    /**
     * Xóa vĩnh viễn các cuộc trò chuyện không có hoạt động mới quá số giờ cấu hình.
     * ngayCapNhat được cập nhật mỗi khi CUSTOMER, BOT, STAFF hoặc ADMIN gửi tin,
     * nên đây là mốc hoạt động cuối cùng của cả hai phía.
     */
    public int deleteInactiveConversations(int inactiveHours) throws SQLException {
        int safeHours = Math.max(1, inactiveHours);
        return inTransaction(entityManager -> {
            // Xóa bảng con trước để không vi phạm khóa ngoại.
            executeUpdate(entityManager, """
                    DELETE FROM TIN_NHAN_HO_TRO
                    WHERE maYC IN (
                        SELECT maYC
                        FROM YEU_CAU_HO_TRO
                        WHERE ngayCapNhat < DATEADD(HOUR, ?, SYSDATETIME())
                    )
                    """, -safeHours);

            return executeUpdate(entityManager, """
                    DELETE FROM YEU_CAU_HO_TRO
                    WHERE ngayCapNhat < DATEADD(HOUR, ?, SYSDATETIME())
                    """, -safeHours);
        });
    }

    private void log(EntityManager entityManager, int staffId, Integer orderId, String action, String content) {
        if (orderId == null) {
            executeUpdate(entityManager, """
                    INSERT INTO NHAT_KY_NHAN_VIEN(maNhanVien,hanhDong,noiDung)
                    VALUES(?,?,?)
                    """, staffId, action, content);
        } else {
            executeUpdate(entityManager, """
                    INSERT INTO NHAT_KY_NHAN_VIEN(maNhanVien,maDH,hanhDong,noiDung)
                    VALUES(?,?,?,?)
                    """, staffId, orderId, action, content);
        }
    }

    private boolean validStatus(String status) {
        return "MOI".equals(status) || "DANG_XU_LY".equals(status)
                || "DA_PHAN_HOI".equals(status) || "DA_DONG".equals(status);
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }
}
