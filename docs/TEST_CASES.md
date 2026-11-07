# Ca kiểm thử Celine Closet

Dữ liệu chuẩn: họ tên QA Khai, email mới duy nhất mỗi ca, điện thoại 0912345678, mật khẩu Abcd123! và xác nhận trùng. Các ca CRUD dùng chung danh mục thử theo thứ tự.

| ID | Tầng | Thao tác | Dữ liệu | Mong đợi | Trạng thái |
|---|---|---|---|---|---|
| TC01 | Controller | POST /register; đặt password và confirmPassword bằng nhau | Ab1!aaa | Báo lỗi quy tắc mật khẩu | Chưa chạy |
| TC02 | Controller | POST /register; đặt password và confirmPassword bằng nhau | Ab1!aaaa | Tạo tài khoản | Chưa chạy |
| TC03 | Controller | POST /register; đặt password và confirmPassword bằng nhau | Ab1!aaaaa | Tạo tài khoản | Chưa chạy |
| TC04 | Controller | POST /register; đặt password và confirmPassword bằng nhau | Ab1!aaaaaaaaaaaaaaaaaaaaaaaaaaa | Tạo tài khoản | Chưa chạy |
| TC05 | Controller | POST /register; đặt password và confirmPassword bằng nhau | Ab1!aaaaaaaaaaaaaaaaaaaaaaaaaaaa | Tạo tài khoản | Chưa chạy |
| TC06 | Controller | POST /register; đặt password và confirmPassword bằng nhau | Ab1!aaaaaaaaaaaaaaaaaaaaaaaaaaaaa | Báo lỗi quy tắc mật khẩu | Chưa chạy |
| TC07 | Controller | POST /register, thay số điện thoại | 091234567 | Báo lỗi số điện thoại | Chưa chạy |
| TC08 | Controller | POST /register, thay số điện thoại | 0912345678 | Tạo tài khoản | Chưa chạy |
| TC09 | Controller | POST /register, thay số điện thoại | 09123456789 | Báo lỗi số điện thoại | Chưa chạy |
| TC10 | Controller | POST /register, thay số điện thoại | 1912345678 | Báo lỗi số điện thoại | Chưa chạy |
| TC11 | UI | Gửi form đăng ký | Email mới, dữ liệu chuẩn | Mở trang đăng nhập | Chưa chạy |
| TC12 | UI | Đăng ký lại | Email đã dùng | Báo email đã tồn tại | Chưa chạy |
| TC13 | UI | Đăng ký | Nhập lại Abcd123@ khác mật khẩu | Báo nhập lại không khớp | Chưa chạy |
| TC14 | UI | Đăng nhập | Email khách đã tạo, đúng mật khẩu | Chuyển /home | Chưa chạy |
| TC15 | UI | Đăng nhập | Email đúng, sai mật khẩu | Hiện lỗi mật khẩu hoặc tài khoản khóa | Chưa chạy |
| TC16 | UI | Đăng nhập | Email mới chưa đăng ký | Báo email chưa có trong hệ thống | Chưa chạy |
| TC17 | UI | Mở /admin/categories bằng khách | Session CUSTOMER | Chặn truy cập quản lý | Chưa chạy |
| TC18 | UI | ADMIN thêm danh mục | Tên QA-TS03226, trạng thái hiện | Danh mục mới xuất hiện | Chưa chạy |
| TC19 | UI | Tải lại danh sách | Tìm QA-TS03226 | Hiển thị đúng tên và trạng thái | Chưa chạy |
| TC20 | UI | Mở sửa dòng vừa tạo rồi lưu | Tên mới QA-TS03226-B | Cùng mã danh mục, hiển thị tên mới | Chưa chạy |
| TC21 | UI | Để trống tên rồi bấm lưu | tenDM rỗng | Trình duyệt yêu cầu nhập tên | Chưa chạy |
| TC22 | UI | Xóa dòng đã sửa rồi tải lại | QA-TS03226-B | Biến mất khỏi danh sách; trong DANH_MUC trạng thái bằng 2 | Chưa chạy |
