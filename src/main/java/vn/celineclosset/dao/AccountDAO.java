package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;
import vn.celineclosset.util.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountDAO extends CrudDAO {

    public Map<String, Object> login(String email, String hash) throws SQLException {
        return queryOne("SELECT * FROM TAI_KHOAN WHERE email=? AND matKhau=? AND trangThai=1", cleanEmail(email), hash);
    }

    public Map<String, Object> findByEmail(String email) throws SQLException {
        return queryOne("SELECT * FROM TAI_KHOAN WHERE email=?", cleanEmail(email));
    }

    public boolean emailExists(String email) throws SQLException {
        return queryOne("SELECT maTK FROM TAI_KHOAN WHERE email=?", cleanEmail(email)) != null;
    }

    public boolean emailExistsForOtherAccount(String email, String maTK) throws SQLException {
        int id = parseId(maTK);
        return queryOne("SELECT maTK FROM TAI_KHOAN WHERE email=? AND maTK<>?", cleanEmail(email), id) != null;
    }

    public void register(String hoTen, String email, String hash, String phone) throws SQLException {
        executeUpdate("INSERT INTO TAI_KHOAN(hoTen,email,matKhau,soDienThoai,vaiTro,trangThai,diemTichLuy,hangThanhVien) VALUES(?,?,?,?, 'CUSTOMER', 1,0,'BRONZE')",
                text(hoTen), cleanEmail(email), hash, text(phone));
    }

    /** Đăng nhập Google: liên kết email cũ hoặc tạo tài khoản khách hàng mới. */
    public Map<String, Object> loginOrCreateGoogle(String googleId, String email, String name, String picture) throws SQLException {
        String normalizedEmail = cleanEmail(email);
        return inTransaction(entityManager -> {
            Map<String, Object> account = queryOne(entityManager,
                    "SELECT * FROM TAI_KHOAN WHERE googleId=? OR email=?", googleId, normalizedEmail);
            if (account == null) {
                String randomPasswordHash = PasswordUtil.hash(UUID.randomUUID().toString());
                executeUpdate(entityManager, """
                        INSERT INTO TAI_KHOAN(hoTen,email,matKhau,googleId,hinhDaiDien,vaiTro,trangThai,diemTichLuy,hangThanhVien)
                        VALUES(?,?,?,?,?,'CUSTOMER',1,0,'BRONZE')
                        """, text(name).isBlank() ? normalizedEmail : text(name), normalizedEmail,
                        randomPasswordHash, googleId, emptyToNull(picture));
            } else {
                int id = ((Number) account.get("maTK")).intValue();
                executeUpdate(entityManager, """
                        UPDATE TAI_KHOAN
                        SET googleId=COALESCE(googleId,?),
                            hoTen=CASE WHEN hoTen IS NULL OR LTRIM(RTRIM(hoTen))='' THEN ? ELSE hoTen END,
                            hinhDaiDien=COALESCE(hinhDaiDien,?), trangThai=1
                        WHERE maTK=?
                        """, googleId, text(name), emptyToNull(picture), id);
            }
            return queryOne(entityManager, "SELECT * FROM TAI_KHOAN WHERE email=? AND trangThai=1", normalizedEmail);
        });
    }

    public List<Map<String, Object>> staff() throws SQLException {
        return staff(true);
    }

    public List<Map<String, Object>> staff(boolean includePrivilegedRoles) throws SQLException {
        return query("""
                SELECT tk.*,
                       (SELECT COUNT(*) FROM DON_HANG dh WHERE dh.maNhanVien=tk.maTK) AS soDonPhuTrach,
                       (SELECT COUNT(*) FROM NHAT_KY_NHAN_VIEN nk WHERE nk.maNhanVien=tk.maTK) AS soHoatDong
                FROM TAI_KHOAN tk
                WHERE tk.vaiTro IN ('STAFF','DELIVERY') AND tk.trangThai<>2
                ORDER BY tk.maTK ASC
                """);
    }

    public List<Map<String, Object>> employeesByRole(String role) throws SQLException {
        String safeRole = "DELIVERY".equals(role) ? "DELIVERY" : "STAFF";
        return query("SELECT * FROM TAI_KHOAN WHERE vaiTro=? AND trangThai=1 ORDER BY hoTen", safeRole);
    }

    public List<Map<String, Object>> deliveryStaff() throws SQLException {
        return employeesByRole("DELIVERY");
    }

    public Map<String, Object> staffById(int maTK) throws SQLException {
        return staffById(maTK, true);
    }

    public Map<String, Object> staffById(int maTK, boolean includePrivilegedRoles) throws SQLException {
        return queryOne("SELECT * FROM TAI_KHOAN WHERE maTK=? AND vaiTro IN ('STAFF','DELIVERY') AND trangThai<>2", maTK);
    }

    public void saveStaff(String maTK, String hoTen, String email, String hash, String phone, String role) throws SQLException {
        int id = parseId(maTK);
        String fullName = text(hoTen);
        String normalizedEmail = cleanEmail(email);
        String cleanPhone = text(phone);
        String cleanRole = "DELIVERY".equals(role) ? "DELIVERY" : "STAFF";

        if (id <= 0) {
            executeUpdate("INSERT INTO TAI_KHOAN(hoTen,email,matKhau,soDienThoai,vaiTro,trangThai,diemTichLuy,hangThanhVien) VALUES(?,?,?,?,?,1,0,'BRONZE')",
                    fullName, normalizedEmail, hash, cleanPhone, cleanRole);
        } else if (hash == null || hash.isBlank()) {
            executeUpdate("UPDATE TAI_KHOAN SET hoTen=?, email=?, soDienThoai=?, vaiTro=? WHERE maTK=? AND vaiTro IN ('STAFF','DELIVERY')",
                    fullName, normalizedEmail, cleanPhone, cleanRole, id);
        } else {
            executeUpdate("UPDATE TAI_KHOAN SET hoTen=?, email=?, matKhau=?, soDienThoai=?, vaiTro=? WHERE maTK=? AND vaiTro IN ('STAFF','DELIVERY')",
                    fullName, normalizedEmail, hash, cleanPhone, cleanRole, id);
        }
    }

    public void toggleStaff(int maTK, int status) throws SQLException {
        toggleStaff(maTK, status, true);
    }

    public void toggleStaff(int maTK, int status, boolean includePrivilegedRoles) throws SQLException {
        executeUpdate("UPDATE TAI_KHOAN SET trangThai=? WHERE maTK=? AND vaiTro IN ('STAFF','DELIVERY')", status, maTK);
    }

    public List<Map<String, Object>> customers(String q) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT tk.*, COUNT(dh.maDH) AS soDonHang,
                       COALESCE(SUM(CASE WHEN dh.trangThai <> N'Đã hủy' THEN dh.tongTien ELSE 0 END),0) AS tongChiTieu
                FROM TAI_KHOAN tk
                LEFT JOIN DON_HANG dh ON tk.maTK=dh.maTK
                WHERE tk.vaiTro='CUSTOMER'
                """);
        List<Object> params = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            String like = "%" + q.trim() + "%";
            sql.append("AND (tk.hoTen LIKE ? OR tk.email LIKE ? OR tk.soDienThoai LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append("GROUP BY tk.maTK, tk.hoTen, tk.email, tk.matKhau, tk.googleId, tk.soDienThoai, tk.diaChiMacDinh, tk.hinhDaiDien, tk.vaiTro, tk.trangThai, tk.diemTichLuy, tk.hangThanhVien, tk.ngayTao ");
        sql.append("ORDER BY tk.maTK DESC");
        return query(sql.toString(), params.toArray());
    }

    public Map<String, Object> customer(int maTK) throws SQLException {
        return queryOne("SELECT * FROM TAI_KHOAN WHERE maTK=? AND vaiTro='CUSTOMER'", maTK);
    }

    public void saveCustomer(String hoTen, String email, String hash, String phone) throws SQLException {
        executeUpdate("INSERT INTO TAI_KHOAN(hoTen,email,matKhau,soDienThoai,vaiTro,trangThai,diemTichLuy,hangThanhVien) VALUES(?,?,?,?, 'CUSTOMER', 1,0,'BRONZE')",
                text(hoTen), cleanEmail(email), hash, text(phone));
    }

    public void toggleCustomer(int maTK, int status) throws SQLException {
        executeUpdate("UPDATE TAI_KHOAN SET trangThai=? WHERE maTK=? AND vaiTro='CUSTOMER'", status, maTK);
    }

    public Map<String, Object> accountById(int maTK) throws SQLException {
        return queryOne("SELECT * FROM TAI_KHOAN WHERE maTK=? AND trangThai=1", maTK);
    }


    public Map<String, Object> staffProfile(int maTK) throws SQLException {
        return queryOne("""
                SELECT tk.*,
                       (SELECT COUNT(*) FROM DON_HANG dh WHERE dh.maNhanVienXuLy=tk.maTK) AS tongDonXuLy,
                       (SELECT COUNT(*) FROM DON_HANG dh WHERE dh.maNhanVienXuLy=tk.maTK AND dh.trangThai=N'Hoàn thành') AS donHoanThanh,
                       (SELECT COUNT(*) FROM DON_HANG dh WHERE dh.maNhanVienXuLy=tk.maTK AND dh.trangThai IN (N'Đã hủy',N'Giao thất bại')) AS donLoi
                FROM TAI_KHOAN tk WHERE tk.maTK=? AND tk.vaiTro IN ('STAFF','DELIVERY')
                """, maTK);
    }

    public List<Map<String,Object>> processedOrders(int maTK) throws SQLException {
        return query("""
                SELECT TOP 50 maDH,ngayDat,hoTenNhan,tongTien,trangThai,phuongThucThanhToan
                FROM DON_HANG WHERE maNhanVienXuLy=? OR maNhanVien=? ORDER BY maDH DESC
                """, maTK, maTK);
    }

    public void updateProfile(int maTK, String hoTen, String email, String phone, String diaChiMacDinh, String hinhDaiDien) throws SQLException {
        executeUpdate("""
                UPDATE TAI_KHOAN
                SET hoTen=?, email=?, soDienThoai=?, diaChiMacDinh=?, hinhDaiDien=COALESCE(?, hinhDaiDien)
                WHERE maTK=?
                """,
                text(hoTen), cleanEmail(email), text(phone), text(diaChiMacDinh), emptyToNull(hinhDaiDien), maTK);
    }

    public void updatePassword(int maTK, String hash) throws SQLException {
        executeUpdate("UPDATE TAI_KHOAN SET matKhau=? WHERE maTK=?", hash, maTK);
    }

    /**
     * Chỉ tạo token quên mật khẩu cho CUSTOMER đang hoạt động.
     * STAFF / DELIVERY / ADMIN nhập email vẫn nhận thông báo chung ở giao diện,
     * nhưng backend tuyệt đối không tạo token và không gửi mail.
     */
    public String createCustomerPasswordResetToken(String email, int minutesValid) throws SQLException {
        Map<String, Object> account = queryOne("""
                SELECT maTK, email, hoTen, vaiTro, trangThai
                FROM TAI_KHOAN
                WHERE email=? AND vaiTro='CUSTOMER' AND trangThai=1
                """, cleanEmail(email));
        if (account == null) {
            return null;
        }

        int accountId = ((Number) account.get("maTK")).intValue();
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String hash = PasswordUtil.hash(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(Math.max(5, minutesValid));

        inTransaction(entityManager -> {
            // Token mới làm toàn bộ token cũ của chính CUSTOMER này hết hiệu lực.
            executeUpdate(entityManager,
                    "UPDATE DAT_LAI_MAT_KHAU SET daDung=1 WHERE maTK=? AND daDung=0", accountId);
            executeUpdate(entityManager, """
                    INSERT INTO DAT_LAI_MAT_KHAU(maTK,tokenHash,hetHan,daDung)
                    VALUES(?,?,?,0)
                    """, accountId, hash, expiresAt);
            return null;
        });
        return rawToken;
    }

    /** Xác minh token chỉ thuộc CUSTOMER, còn hạn và chưa dùng. */
    public Map<String, Object> validCustomerPasswordReset(String rawToken) throws SQLException {
        if (rawToken == null || rawToken.isBlank()) return null;
        return queryOne("""
                SELECT TOP 1 r.maToken, r.maTK, r.hetHan, tk.email, tk.hoTen
                FROM DAT_LAI_MAT_KHAU r
                JOIN TAI_KHOAN tk ON tk.maTK=r.maTK
                WHERE r.tokenHash=?
                  AND r.daDung=0
                  AND r.hetHan>SYSDATETIME()
                  AND tk.trangThai=1
                  AND tk.vaiTro='CUSTOMER'
                ORDER BY r.maToken DESC
                """, PasswordUtil.hash(rawToken));
    }

    /** Đổi mật khẩu đúng CUSTOMER của token và khóa token ngay sau khi dùng. */
    public boolean resetCustomerPassword(String rawToken, String newHash) throws SQLException {
        if (rawToken == null || rawToken.isBlank()) return false;
        return inTransaction(entityManager -> {
            Map<String, Object> row = queryOne(entityManager, """
                    SELECT TOP 1 r.maToken, r.maTK
                    FROM DAT_LAI_MAT_KHAU r WITH (UPDLOCK, ROWLOCK)
                    JOIN TAI_KHOAN tk ON tk.maTK=r.maTK
                    WHERE r.tokenHash=?
                      AND r.daDung=0
                      AND r.hetHan>SYSDATETIME()
                      AND tk.trangThai=1
                      AND tk.vaiTro='CUSTOMER'
                    ORDER BY r.maToken DESC
                    """, PasswordUtil.hash(rawToken));
            if (row == null) return false;

            int tokenId = ((Number) row.get("maToken")).intValue();
            int accountId = ((Number) row.get("maTK")).intValue();
            int updated = executeUpdate(entityManager,
                    "UPDATE TAI_KHOAN SET matKhau=? WHERE maTK=? AND vaiTro='CUSTOMER' AND trangThai=1",
                    newHash, accountId);
            if (updated != 1) return false;

            // Một token chỉ được sử dụng đúng một lần; đồng thời vô hiệu hóa mọi token còn lại.
            executeUpdate(entityManager,
                    "UPDATE DAT_LAI_MAT_KHAU SET daDung=1 WHERE maTK=? AND daDung=0", accountId);
            return true;
        });
    }

    private int parseId(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String cleanEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
