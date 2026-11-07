package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class NewsDAO extends CrudDAO {
    private final NewsCategoryDAO categoryDAO = new NewsCategoryDAO();
    private static final Object DEMO_NEWS_LOCK = new Object();
    private static volatile boolean demoNewsReady;

    public List<Map<String, Object>> all(String q) throws SQLException {
        ensureDemoNews();
        if (q == null || q.isBlank()) {
            return query("""
                    SELECT tt.*, lt.tenLoai
                    FROM TIN_TUC tt
                    LEFT JOIN LOAI_TIN_TUC lt ON tt.maLoaiTin=lt.maLoaiTin
                    ORDER BY tt.ngayCapNhat DESC, tt.maTin DESC
                    """);
        }
        String like = "%" + q.trim() + "%";
        return query("""
                SELECT tt.*, lt.tenLoai
                FROM TIN_TUC tt
                LEFT JOIN LOAI_TIN_TUC lt ON tt.maLoaiTin=lt.maLoaiTin
                WHERE tt.tieuDe LIKE ? OR tt.tomTat LIKE ? OR tt.noiDung LIKE ? OR lt.tenLoai LIKE ?
                ORDER BY tt.ngayCapNhat DESC, tt.maTin DESC
                """, like, like, like, like);
    }

    public List<Map<String, Object>> published(String categoryId) throws SQLException {
        ensureDemoNews();
        if (categoryId == null || categoryId.isBlank()) {
            return query("""
                    SELECT tt.*, lt.tenLoai
                    FROM TIN_TUC tt
                    LEFT JOIN LOAI_TIN_TUC lt ON tt.maLoaiTin=lt.maLoaiTin
                    WHERE tt.trangThai=1 AND (lt.trangThai=1 OR tt.maLoaiTin IS NULL)
                    ORDER BY tt.ngayCapNhat DESC, tt.maTin DESC
                    """);
        }
        int parsedCategoryId;
        try {
            parsedCategoryId = Integer.parseInt(categoryId.trim());
        } catch (NumberFormatException e) {
            return published(null);
        }
        return query("""
                SELECT tt.*, lt.tenLoai
                FROM TIN_TUC tt
                LEFT JOIN LOAI_TIN_TUC lt ON tt.maLoaiTin=lt.maLoaiTin
                WHERE tt.trangThai=1 AND tt.maLoaiTin=? AND lt.trangThai=1
                ORDER BY tt.ngayCapNhat DESC, tt.maTin DESC
                """, parsedCategoryId);
    }

    public Map<String, Object> byId(int id) throws SQLException {
        ensureDemoNews();
        return queryOne("""
                SELECT tt.*, lt.tenLoai
                FROM TIN_TUC tt
                LEFT JOIN LOAI_TIN_TUC lt ON tt.maLoaiTin=lt.maLoaiTin
                WHERE tt.maTin=?
                """, id);
    }

    /** Chỉ trả bài viết đang được phép hiển thị ở phía khách hàng. */
    public Map<String, Object> publishedById(int id) throws SQLException {
        ensureDemoNews();
        return queryOne("""
                SELECT tt.*, lt.tenLoai
                FROM TIN_TUC tt
                LEFT JOIN LOAI_TIN_TUC lt ON tt.maLoaiTin=lt.maLoaiTin
                WHERE tt.maTin=? AND tt.trangThai=1
                  AND (lt.trangThai=1 OR tt.maLoaiTin IS NULL)
                """, id);
    }

    public void save(String idValue, String title, String summary, String content,
                     String image, int categoryId, int status, int authorId) throws SQLException {
        categoryDAO.ensureSchema();
        int id = parseId(idValue);
        String cleanImage = image == null || image.isBlank() ? null : image.trim();
        Integer cleanCategoryId = categoryId > 0 ? categoryId : null;
        if (id <= 0) {
            executeUpdate("""
                    INSERT INTO TIN_TUC(tieuDe,tomTat,noiDung,hinhAnh,maLoaiTin,trangThai,maNguoiTao,ngayTao,ngayCapNhat)
                    VALUES(?,?,?,?,?,?,?,SYSDATETIME(),SYSDATETIME())
                    """, text(title), text(summary), text(content), cleanImage, cleanCategoryId, status, authorId);
        } else {
            executeUpdate("""
                    UPDATE TIN_TUC
                    SET tieuDe=?, tomTat=?, noiDung=?, hinhAnh=?, maLoaiTin=?, trangThai=?, ngayCapNhat=SYSDATETIME()
                    WHERE maTin=?
                    """, text(title), text(summary), text(content), cleanImage, cleanCategoryId, status, id);
        }
    }

    public void toggle(int id, int status) throws SQLException {
        categoryDAO.ensureSchema();
        executeUpdate("UPDATE TIN_TUC SET trangThai=?, ngayCapNhat=SYSDATETIME() WHERE maTin=?", status, id);
    }

    public void delete(int id) throws SQLException {
        categoryDAO.ensureSchema();
        executeUpdate("DELETE FROM TIN_TUC WHERE maTin=?", id);
    }

    /**
     * Database cũ có thể đã có bảng tin nhưng chưa có bài nào. Ở chế độ demo,
     * tự bổ sung một bộ bài mẫu có nội dung đầy đủ và không tạo trùng khi restart.
     */
    public void ensureDemoNews() throws SQLException {
        categoryDAO.ensureSchema();
        if (demoNewsReady) return;
        synchronized (DEMO_NEWS_LOCK) {
            if (demoNewsReady) return;
            executeUpdate("""
                    DECLARE @admin INT=(SELECT TOP 1 maTK FROM TAI_KHOAN WHERE vaiTro='ADMIN' AND trangThai=1 ORDER BY maTK);
                    IF @admin IS NOT NULL
                    BEGIN
                      IF NOT EXISTS(SELECT 1 FROM TIN_TUC WHERE tieuDe=N'5 cách phối blazer thanh lịch cho tuần làm việc')
                        INSERT INTO TIN_TUC(tieuDe,tomTat,noiDung,hinhAnh,maLoaiTin,trangThai,maNguoiTao)
                        SELECT N'5 cách phối blazer thanh lịch cho tuần làm việc',
                               N'Từ quần tây, chân váy đến jeans: 5 công thức phối blazer dễ áp dụng mỗi ngày.',
                               N'Blazer là món đồ cốt lõi của tủ đồ công sở. Thứ Hai có thể phối blazer cùng sơ mi trắng và quần tây ống rộng; giữa tuần đổi sang áo thun ôm và jeans cạp cao; cuối tuần kết hợp chân váy midi để tổng thể mềm mại hơn. Ưu tiên bảng màu trung tính và thêm một phụ kiện nhỏ để trang phục có điểm nhấn mà vẫn gọn gàng.',
                               N'assets/images/fashion/hero-02.jpg',
                               (SELECT TOP 1 maLoaiTin FROM LOAI_TIN_TUC WHERE tenLoai=N'Phong cách' AND trangThai=1),1,@admin;
                      IF NOT EXISTS(SELECT 1 FROM TIN_TUC WHERE tieuDe=N'Hướng dẫn chọn size trang phục công sở')
                        INSERT INTO TIN_TUC(tieuDe,tomTat,noiDung,hinhAnh,maLoaiTin,trangThai,maNguoiTao)
                        SELECT N'Hướng dẫn chọn size trang phục công sở',
                               N'Ba số đo cần biết trước khi chọn áo, quần hoặc đầm trên Celine Closet.',
                               N'Để chọn size chính xác, hãy đo vòng ngực, vòng eo và vòng mông bằng thước dây khi cơ thể ở trạng thái tự nhiên. Với blazer nên ưu tiên phần vai vừa vặn; với quần tây hãy chú ý vòng eo và mông; với đầm cần kiểm tra cả ngực lẫn eo. Nếu số đo nằm giữa hai size, hãy đọc mô tả chất liệu để biết sản phẩm có co giãn hay không và nhắn C&C Assistant để được gợi ý thêm.',
                               N'assets/images/fashion/story.jpg',
                               (SELECT TOP 1 maLoaiTin FROM LOAI_TIN_TUC WHERE tenLoai=N'Phong cách' AND trangThai=1),1,@admin;
                      IF NOT EXISTS(SELECT 1 FROM TIN_TUC WHERE tieuDe=N'Mid-season sale – ưu đãi đến 50%')
                        INSERT INTO TIN_TUC(tieuDe,tomTat,noiDung,hinhAnh,maLoaiTin,trangThai,maNguoiTao)
                        SELECT N'Mid-season sale – ưu đãi đến 50%',
                               N'Ưu đãi giữa mùa cho các thiết kế được chọn và voucher thành viên.',
                               N'Chương trình Mid-season Sale áp dụng cho một số sản phẩm được chọn. Thành viên đăng nhập có thể sử dụng voucher phù hợp tại bước thanh toán và vẫn được tích điểm sau khi đơn hoàn thành. Số lượng sản phẩm ưu đãi có giới hạn theo tồn kho.',
                               N'assets/images/fashion/hero-03.jpg',
                               (SELECT TOP 1 maLoaiTin FROM LOAI_TIN_TUC WHERE tenLoai=N'Khuyến mãi' AND trangThai=1),1,@admin;
                      IF NOT EXISTS(SELECT 1 FROM TIN_TUC WHERE tieuDe=N'Office Edit – bảng màu trung tính cho mùa mới')
                        INSERT INTO TIN_TUC(tieuDe,tomTat,noiDung,hinhAnh,maLoaiTin,trangThai,maNguoiTao)
                        SELECT N'Office Edit – bảng màu trung tính cho mùa mới',
                               N'Kem, nâu, xám và navy tạo nên bộ sưu tập dễ phối cho nhịp sống văn phòng.',
                               N'Office Edit tập trung vào những phom dáng có thể sử dụng nhiều lần: blazer chiết eo, sơ mi mềm rủ, quần tây cạp cao và đầm midi. Bảng màu trung tính giúp các món đồ phối chéo với nhau, giảm thời gian chọn trang phục nhưng vẫn giữ vẻ chỉn chu.',
                               N'assets/images/fashion/hero-04.jpg',
                               (SELECT TOP 1 maLoaiTin FROM LOAI_TIN_TUC WHERE tenLoai=N'Bộ sưu tập' AND trangThai=1),1,@admin;
                      IF NOT EXISTS(SELECT 1 FROM TIN_TUC WHERE tieuDe=N'Chính sách đổi sản phẩm trong 7 ngày')
                        INSERT INTO TIN_TUC(tieuDe,tomTat,noiDung,hinhAnh,maLoaiTin,trangThai,maNguoiTao)
                        SELECT N'Chính sách đổi sản phẩm trong 7 ngày',
                               N'Điều kiện cơ bản để gửi yêu cầu đổi hoặc trả sản phẩm sau khi nhận hàng.',
                               N'Khách hàng có thể gửi yêu cầu trong thời hạn hiển thị trên đơn hàng sau khi đơn hoàn thành. Sản phẩm cần còn nguyên tình trạng theo chính sách của cửa hàng. Trên trang lịch sử đơn, khách có thể theo dõi toàn bộ tiến trình trả hàng và thông tin hoàn tiền nếu yêu cầu được chấp nhận.',
                               N'assets/images/fashion/card-01.jpg',
                               (SELECT TOP 1 maLoaiTin FROM LOAI_TIN_TUC WHERE tenLoai=N'Cửa hàng' AND trangThai=1),1,@admin;
                    END
                    """);
            demoNewsReady = true;
        }
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
