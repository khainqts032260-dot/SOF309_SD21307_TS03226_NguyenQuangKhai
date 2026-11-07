package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Quản lý các loại tin tức như Bộ sưu tập, Khuyến mãi, Cửa hàng. */
public class NewsCategoryDAO extends CrudDAO {

    /**
     * Tự bổ sung bảng/cột khi dự án được chạy trên database cũ.
     * Nhờ vậy chức năng mới không làm hỏng dữ liệu tin tức đang có.
     */
    public void ensureSchema() throws SQLException {
        executeUpdate("""
                IF OBJECT_ID(N'dbo.LOAI_TIN_TUC', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.LOAI_TIN_TUC (
                        maLoaiTin INT IDENTITY(1,1) PRIMARY KEY,
                        tenLoai NVARCHAR(120) NOT NULL,
                        moTa NVARCHAR(400) NULL,
                        trangThai TINYINT NOT NULL DEFAULT 1,
                        CONSTRAINT CK_LOAI_TIN_TUC_TRANG_THAI CHECK (trangThai IN (0,1,2))
                    )
                END
                """);
        executeUpdate("""
                IF COL_LENGTH('dbo.TIN_TUC', 'maLoaiTin') IS NULL
                    ALTER TABLE dbo.TIN_TUC ADD maLoaiTin INT NULL
                """);
        executeUpdate("""
                IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name='FK_TIN_TUC_LOAI_TIN')
                    ALTER TABLE dbo.TIN_TUC ADD CONSTRAINT FK_TIN_TUC_LOAI_TIN
                    FOREIGN KEY (maLoaiTin) REFERENCES dbo.LOAI_TIN_TUC(maLoaiTin)
                """);
        executeUpdate("""
                IF NOT EXISTS (SELECT 1 FROM dbo.LOAI_TIN_TUC WHERE trangThai<>2)
                BEGIN
                    INSERT INTO dbo.LOAI_TIN_TUC(tenLoai,moTa,trangThai) VALUES
                    (N'Bộ sưu tập',N'Thông tin về bộ sưu tập và lookbook mới.',1),
                    (N'Khuyến mãi',N'Chương trình ưu đãi, voucher và sự kiện bán hàng.',1),
                    (N'Cửa hàng',N'Thông báo vận hành, chính sách và cập nhật từ Celine Closet.',1),
                    (N'Phong cách',N'Gợi ý phối đồ và xu hướng công sở.',1)
                END
                """);
    }

    public List<Map<String, Object>> all(boolean activeOnly) throws SQLException {
        ensureSchema();
        return query(activeOnly
                ? "SELECT * FROM LOAI_TIN_TUC WHERE trangThai=1 ORDER BY tenLoai"
                : "SELECT * FROM LOAI_TIN_TUC WHERE trangThai<>2 ORDER BY maLoaiTin DESC");
    }

    public Map<String, Object> byId(int id) throws SQLException {
        ensureSchema();
        return queryOne("SELECT * FROM LOAI_TIN_TUC WHERE maLoaiTin=? AND trangThai<>2", id);
    }

    public void save(String idValue, String name, String description, int status) throws SQLException {
        ensureSchema();
        int id = parseId(idValue);
        if (id <= 0) {
            executeUpdate("INSERT INTO LOAI_TIN_TUC(tenLoai,moTa,trangThai) VALUES(?,?,?)",
                    text(name), text(description), status);
        } else {
            executeUpdate("UPDATE LOAI_TIN_TUC SET tenLoai=?,moTa=?,trangThai=? WHERE maLoaiTin=?",
                    text(name), text(description), status, id);
        }
    }

    public void setStatus(int id, int status) throws SQLException {
        ensureSchema();
        executeUpdate("UPDATE LOAI_TIN_TUC SET trangThai=? WHERE maLoaiTin=?", status, id);
    }

    public void delete(int id) throws SQLException {
        setStatus(id, 2);
    }

    private int parseId(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }
}
