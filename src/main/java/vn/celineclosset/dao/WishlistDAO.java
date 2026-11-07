package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lưu danh sách sản phẩm yêu thích theo từng tài khoản khách hàng. */
public class WishlistDAO extends CrudDAO {
    private volatile boolean tableReady;

    private void ensureTable() throws SQLException {
        if (tableReady) return;
        synchronized (this) {
            if (tableReady) return;
            executeUpdate("""
                    IF OBJECT_ID(N'dbo.SAN_PHAM_YEU_THICH', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.SAN_PHAM_YEU_THICH (
                            maTK INT NOT NULL,
                            maSP INT NOT NULL,
                            ngayThem DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            CONSTRAINT PK_SAN_PHAM_YEU_THICH PRIMARY KEY (maTK, maSP),
                            CONSTRAINT FK_YEU_THICH_TAI_KHOAN FOREIGN KEY (maTK)
                                REFERENCES dbo.TAI_KHOAN(maTK),
                            CONSTRAINT FK_YEU_THICH_SAN_PHAM FOREIGN KEY (maSP)
                                REFERENCES dbo.SAN_PHAM(maSP)
                        );
                        CREATE INDEX IX_SAN_PHAM_YEU_THICH_NGAY
                            ON dbo.SAN_PHAM_YEU_THICH(maTK, ngayThem DESC);
                    END
                    """);
            tableReady = true;
        }
    }

    public int count(int accountId) throws SQLException {
        ensureTable();
        Map<String, Object> row = queryOne(
                "SELECT COUNT(*) AS total FROM dbo.SAN_PHAM_YEU_THICH WHERE maTK=?", accountId);
        return row == null ? 0 : ((Number) row.get("total")).intValue();
    }

    public Map<Integer, Boolean> productIdMap(int accountId) throws SQLException {
        ensureTable();
        Map<Integer, Boolean> result = new LinkedHashMap<>();
        for (Map<String, Object> row : query(
                "SELECT maSP FROM dbo.SAN_PHAM_YEU_THICH WHERE maTK=?", accountId)) {
            result.put(((Number) row.get("maSP")).intValue(), Boolean.TRUE);
        }
        return result;
    }

    public List<Map<String, Object>> products(int accountId) throws SQLException {
        ensureTable();
        return query("""
                SELECT sp.*, dm.tenDM, yt.ngayThem
                FROM dbo.SAN_PHAM_YEU_THICH yt
                INNER JOIN dbo.SAN_PHAM sp ON sp.maSP=yt.maSP
                LEFT JOIN dbo.DANH_MUC dm ON dm.maDM=sp.maDM
                WHERE yt.maTK=? AND sp.trangThai=1
                ORDER BY yt.ngayThem DESC
                """, accountId);
    }

    public boolean toggle(int accountId, int productId) throws SQLException {
        ensureTable();
        return inTransaction(entityManager -> {
            Map<String, Object> existing = queryOne(entityManager,
                    "SELECT maSP FROM dbo.SAN_PHAM_YEU_THICH WHERE maTK=? AND maSP=?",
                    accountId, productId);
            if (existing != null) {
                executeUpdate(entityManager,
                        "DELETE FROM dbo.SAN_PHAM_YEU_THICH WHERE maTK=? AND maSP=?",
                        accountId, productId);
                return false;
            }
            Map<String, Object> product = queryOne(entityManager,
                    "SELECT maSP FROM dbo.SAN_PHAM WHERE maSP=? AND trangThai=1", productId);
            if (product == null) throw new SQLException("Sản phẩm không tồn tại hoặc đang bị ẩn.");
            executeUpdate(entityManager,
                    "INSERT INTO dbo.SAN_PHAM_YEU_THICH(maTK,maSP) VALUES(?,?)",
                    accountId, productId);
            return true;
        });
    }

    public void remove(int accountId, int productId) throws SQLException {
        ensureTable();
        executeUpdate("DELETE FROM dbo.SAN_PHAM_YEU_THICH WHERE maTK=? AND maSP=?",
                accountId, productId);
    }
}
