package com.poly.sof309.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================================
 * [Y3] FILE TEST MẪU BẰNG JUNIT 5 CHO CONTROLLER
 * Sinh viên tham khảo file này để viết unit test cho các hàm trong Controller/Service
 * dựa theo bộ Test Case đã thiết kế ở Y2 (trong file docs/TEST_CASES.md).
 * =========================================================================
 */
public class SampleControllerTest {

    @Test
    @DisplayName("TC_AUTH_01: Kiểm tra đăng nhập với thông tin hợp lệ")
    public void testLoginSuccess() {
        // Giả lập logic kiểm tra đăng nhập
        String username = "admin";
        String password = "123";
        
        boolean isValidUser = authenticate(username, password);
        assertTrue(isValidUser, "Đăng nhập phải thành công với tài khoản đúng");
    }

    @Test
    @DisplayName("TC_AUTH_02: Kiểm tra đăng nhập thất bại khi để trống mật khẩu")
    public void testLoginEmptyPassword() {
        String username = "admin";
        String password = "";

        boolean isValidUser = authenticate(username, password);
        assertFalse(isValidUser, "Đăng nhập phải thất bại khi mật khẩu trống");
    }

    // Hàm giả lập (Sinh viên thay bằng gọi trực tiếp Controller / Service của dự án web)
    private boolean authenticate(String user, String pass) {
        return "admin".equals(user) && "123".equals(pass);
    }
}

