# ĐẶC TẢ NGHIỆP VỤ WEB CÁ NHÂN (SPECIFICATIONS)
> *Dùng làm ngữ cảnh (Context) gửi cho AI để sinh Test Plan & Test Case chính xác theo dự án riêng của bạn.*

---

## 1. THÔNG TIN DỰ ÁN
* **Tên đề tài:** [Ví dụ: Website Bán Hàng Trực Tuyến]
* **Công nghệ:** Java 17, Spring Boot, MySQL / H2
* **URL chạy local:** `http://localhost:8080`

## 2. QUY TẮC NGHIỆP VỤ (BUSINESS RULES)
* **Chức năng Đăng ký:** Form gồm `username` (5-20 ký tự), `email`, `password` (tối thiểu 8 ký tự, có ký tự đặc biệt), `confirmPassword`.
* **Chức năng Đăng nhập:** Kiểm tra đúng tài khoản/mật khẩu $\rightarrow$ chuyển hướng về trang chủ; sai $\rightarrow$ báo lỗi.
* **Chức năng CRUD kiểm thử (Ví dụ: Sản phẩm):**
  * Thêm mới: Tên sản phẩm (bắt buộc, không trùng), Giá bán (> 0), Số lượng (>= 0).
  * Sửa / Xóa: Có xác nhận trước khi xóa, cập nhật dữ liệu vào DB.
