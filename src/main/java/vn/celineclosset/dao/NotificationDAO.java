package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Thông báo nội bộ cho tài khoản khách hàng và nhân viên. */
public class NotificationDAO extends CrudDAO {
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean schemaReady;

    public void ensureSchema() throws SQLException {
        if (schemaReady) return;
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) return;
            executeUpdate("""
                    IF OBJECT_ID(N'dbo.THONG_BAO_TAI_KHOAN', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.THONG_BAO_TAI_KHOAN (
                            maTB INT IDENTITY(1,1) PRIMARY KEY,
                            maTK INT NOT NULL,
                            tieuDe NVARCHAR(180) NOT NULL,
                            noiDung NVARCHAR(1000) NOT NULL,
                            duongDan VARCHAR(500) NULL,
                            loai VARCHAR(40) NOT NULL DEFAULT 'SYSTEM',
                            daDoc BIT NOT NULL DEFAULT 0,
                            ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            CONSTRAINT FK_TBTK_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK)
                        );
                        CREATE INDEX IX_TBTK_TAI_KHOAN ON dbo.THONG_BAO_TAI_KHOAN(maTK,daDoc,ngayTao DESC);
                    END
                    """);
            schemaReady = true;
        }
    }

    public void create(int accountId, String title, String content, String path, String type) throws SQLException {
        ensureSchema();
        executeUpdate("""
                INSERT INTO THONG_BAO_TAI_KHOAN(maTK,tieuDe,noiDung,duongDan,loai)
                VALUES(?,?,?,?,?)
                """, accountId, clean(title), clean(content), cleanNullable(path), cleanType(type));
    }

    public List<Map<String, Object>> unread(int accountId, int limit) throws SQLException {
        ensureSchema();
        int safeLimit = Math.max(1, Math.min(20, limit));
        return query("SELECT TOP " + safeLimit + " * FROM THONG_BAO_TAI_KHOAN WHERE maTK=? AND daDoc=0 ORDER BY ngayTao DESC,maTB DESC", accountId);
    }

    public int unreadCount(int accountId) throws SQLException {
        ensureSchema();
        Map<String, Object> row = queryOne("SELECT COUNT(*) AS total FROM THONG_BAO_TAI_KHOAN WHERE maTK=? AND daDoc=0", accountId);
        return row == null ? 0 : ((Number) row.get("total")).intValue();
    }

    public void markRead(int accountId, List<Map<String, Object>> notifications) throws SQLException {
        ensureSchema();
        if (notifications == null || notifications.isEmpty()) return;
        for (Map<String, Object> notification : notifications) {
            Object id = notification.get("maTB");
            if (id instanceof Number number) {
                executeUpdate("UPDATE THONG_BAO_TAI_KHOAN SET daDoc=1 WHERE maTB=? AND maTK=?", number.intValue(), accountId);
            }
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? "Thông báo" : value.trim();
    }

    private String cleanNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String cleanType(String value) {
        String clean = value == null ? "SYSTEM" : value.trim().toUpperCase();
        return clean.length() > 40 ? clean.substring(0, 40) : clean;
    }
}
