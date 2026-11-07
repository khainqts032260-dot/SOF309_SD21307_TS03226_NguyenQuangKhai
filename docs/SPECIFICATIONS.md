# Đặc tả Celine Closet
Nguyễn Quang Khải · SD2001 · TS03226

Ứng dụng Java 17, Servlet/JSP, Hibernate và SQL Server. Các đường dẫn dưới đây tính từ context của ứng dụng trên Tomcat 11 tại localhost:8080.

| Màn hình | Trường và quy tắc | Kết quả |
|---|---|---|
| /register | hoTen, email, soDienThoai, password, confirmPassword. Email được bỏ khoảng trắng hai đầu, chuyển chữ thường, kiểm tra định dạng và trùng. Điện thoại gồm 10 chữ số bắt đầu bằng 0. Mật khẩu dài 8–32 ký tự, đủ chữ hoa, chữ thường, số và ký tự thuộc nhóm !@#$%^&*. Nhập lại phải khớp. | Tạo tài khoản và hiển thị trang đăng nhập. Dữ liệu sai hiển thị lỗi trên form. |
| /login | email, password. Kiểm tra định dạng email, tài khoản tồn tại, mật khẩu và trạng thái tài khoản. | Ghi người dùng vào session auth. Khách đi tới /home, hoặc /wishlist nếu next=wishlist. Nhân sự đi tới trang theo vai trò. |
| /admin/categories | Chỉ ADMIN. Form có maDM khi sửa, tên tenDM bắt buộc trên giao diện, mô tả moTa và trạng thái trangThai. | Thêm, đọc danh sách, sửa theo mã, ẩn hoặc hiện. Xóa mềm đặt trangThai=2 và loại khỏi danh sách. |
