# BỘ KỊCH BẢN KIỂM THỬ (TEST CASES)

| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại test (Pos/Neg) | Các bước thực hiện | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi | Trạng thái (Pass/Fail) |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_AUTH_01** | Login | Đăng nhập thành công với tài khoản đúng | Positive | 1. Nhập username<br>2. Nhập password<br>3. Bấm Đăng nhập | `admin` / `123456` | Chuyển hướng tới trang chủ | Pass |
| **TC_AUTH_02** | Login | Đăng nhập thất bại khi để trống mật khẩu | Negative | 1. Nhập username<br>2. Bỏ trống password<br>3. Bấm Đăng nhập | `admin` / `""` | Báo lỗi "Mật khẩu không được để trống" | Pass |
| **TC_CRUD_01** | CRUD | Thêm mới bản ghi thành công | Positive | 1. Mở form thêm mới<br>2. Điền thông tin hợp lệ<br>3. Bấm Lưu | Dữ liệu hợp lệ | Lưu thành công, dữ liệu hiển thị trên bảng | Pass |
| **TC_CRUD_02** | CRUD | Thêm mới thất bại khi dữ liệu không hợp lệ | Negative | 1. Mở form thêm mới<br>2. Nhập sai định dạng/để trống<br>3. Bấm Lưu | Dữ liệu sai | Báo lỗi validation, không lưu vào DB | Pass |

