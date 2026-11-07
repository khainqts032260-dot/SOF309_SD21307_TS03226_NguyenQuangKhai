package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Tích điểm, hạng thành viên, voucher cá nhân và đổi quà. */
public class LoyaltyDAO extends CrudDAO {

    public Map<String, Object> summary(int accountId) throws SQLException {
        return queryOne("""
                SELECT maTK,hoTen,email,diemTichLuy,hangThanhVien,
                       CASE hangThanhVien WHEN 'BRONZE' THEN 300 WHEN 'SILVER' THEN 1000 WHEN 'GOLD' THEN 3000 WHEN 'PLATINUM' THEN 7000 ELSE diemTichLuy END AS mocTiepTheo
                FROM TAI_KHOAN WHERE maTK=? AND vaiTro='CUSTOMER'
                """, accountId);
    }

    public List<Map<String, Object>> vouchers(int accountId) throws SQLException {
        return query("""
                SELECT v.*,khv.maKHV,khv.trangThai AS trangThaiCaNhan,khv.ngayNhan,khv.ngaySuDung
                FROM KHACH_HANG_VOUCHER khv JOIN VOUCHER v ON v.maVoucher=khv.maVoucher
                WHERE khv.maTK=? ORDER BY CASE khv.trangThai WHEN 'AVAILABLE' THEN 0 ELSE 1 END,khv.maKHV DESC
                """, accountId);
    }

    public List<Map<String, Object>> publicVouchers() throws SQLException {
        return query("""
                SELECT * FROM VOUCHER WHERE trangThai=1 AND diemDoi=0
                  AND ngayBatDau<=SYSDATETIME() AND (ngayKetThuc IS NULL OR ngayKetThuc>=SYSDATETIME())
                  AND (soLuot IS NULL OR daDung<soLuot)
                ORDER BY donToiThieu ASC
                """);
    }

    public List<Map<String, Object>> rewards() throws SQLException {
        return query("""
                SELECT pt.*,v.maCode,v.tenVoucher FROM PHAN_THUONG pt
                LEFT JOIN VOUCHER v ON v.maVoucher=pt.maVoucher WHERE pt.trangThai=1 ORDER BY pt.diemCan ASC
                """);
    }

    public List<Map<String, Object>> pointHistory(int accountId) throws SQLException {
        return query("SELECT TOP 30 * FROM LICH_SU_DIEM WHERE maTK=? ORDER BY maLSD DESC", accountId);
    }

    public List<Map<String, Object>> redemptionHistory(int accountId) throws SQLException {
        return query("""
                SELECT dt.*,pt.tenPhanThuong,pt.loai FROM DOI_THUONG dt
                JOIN PHAN_THUONG pt ON pt.maPhanThuong=dt.maPhanThuong
                WHERE dt.maTK=? ORDER BY dt.maDoiThuong DESC
                """, accountId);
    }

    public String redeem(int accountId, int rewardId) throws SQLException {
        return inTransaction(entityManager -> redeemInTransaction(entityManager, accountId, rewardId));
    }

    private String redeemInTransaction(EntityManager entityManager, int accountId, int rewardId) throws SQLException {
        Map<String, Object> account = queryOne(entityManager,
                "SELECT diemTichLuy FROM TAI_KHOAN WITH (UPDLOCK,ROWLOCK) WHERE maTK=? AND vaiTro='CUSTOMER' AND trangThai=1",
                accountId);
        Map<String, Object> reward = queryOne(entityManager,
                "SELECT * FROM PHAN_THUONG WHERE maPhanThuong=? AND trangThai=1", rewardId);
        if (account == null || reward == null) throw new SQLException("Tài khoản hoặc phần thưởng không hợp lệ.");
        int points = ((Number) account.get("diemTichLuy")).intValue();
        int required = ((Number) reward.get("diemCan")).intValue();
        if (points < required) throw new SQLException("Bạn chưa đủ điểm để đổi phần thưởng này.");

        executeUpdate(entityManager, "UPDATE TAI_KHOAN SET diemTichLuy=diemTichLuy-? WHERE maTK=?", required, accountId);
        executeUpdate(entityManager, """
                INSERT INTO LICH_SU_DIEM(maTK,soDiem,loai,noiDung)
                VALUES(?,?,'REDEEM',N'Đổi phần thưởng thành viên')
                """, accountId, -required);
        executeUpdate(entityManager, """
                INSERT INTO DOI_THUONG(maTK,maPhanThuong,diemDaDung,trangThai,ghiChu)
                VALUES(?,?,?,'PENDING',N'Yêu cầu đổi thưởng từ website')
                """, accountId, rewardId, required);

        if ("VOUCHER".equals(String.valueOf(reward.get("loai"))) && reward.get("maVoucher") != null) {
            int voucherId = ((Number) reward.get("maVoucher")).intValue();
            executeUpdate(entityManager, """
                    INSERT INTO KHACH_HANG_VOUCHER(maTK,maVoucher,trangThai) VALUES(?,?,'AVAILABLE')
                    """, accountId, voucherId);
        }
        refreshTier(entityManager, accountId);
        return String.valueOf(reward.get("tenPhanThuong"));
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
}
