# Kế hoạch kiểm thử tuần 1
Nguyễn Quang Khải · TS03226 · SD2001

Mục tiêu là xác nhận khách có thể tạo tài khoản, đăng nhập và quản trị viên quản lý danh mục. Dùng Eclipse, Java 17, Tomcat 11 và bản dữ liệu thử CelineClossetDB trên SQL Server. Chuẩn bị một ADMIN, một khách đang hoạt động và email mới cho mỗi lần đăng ký.

Kiểm tra quy tắc tại AuthController, ValidationUtil, AdminCategoryController và kết quả trên JSP. Phân vùng dữ liệu theo định dạng, tài khoản tồn tại và vai trò. Kiểm tra biên điện thoại và mật khẩu. Dùng danh mục riêng để quan sát thay đổi qua từng bước CRUD. Thanh toán và vận chuyển ngoài phạm vi tuần này.

Nghiệm thu Y1 khi mở được web và thực hiện thành công đăng ký, đăng nhập và CRUD. Ghi kết quả thực tế khi chạy, kèm lỗi và bước tái hiện nếu khác mong đợi. Tuần 1 thiết kế ca cho controller và giao diện; triển khai tự động theo lộ trình các tuần sau.
