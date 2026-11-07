package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.util.AppConfig;
import vn.celineclosset.util.MailUtil;
import vn.celineclosset.util.PasswordUtil;
import vn.celineclosset.util.ValidationUtil;

import java.io.IOException;
import java.util.Map;

@WebServlet({"/forgot-password", "/reset-password"})
public class PasswordResetController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/reset-password".equals(req.getServletPath())) {
                String token = req.getParameter("token");
                req.setAttribute("token", token);
                req.setAttribute("validReset", accountDAO.validCustomerPasswordReset(token) != null);
                view(req, resp, "reset-password.jsp");
            } else {
                req.setAttribute("passwordResetMinutes", Math.max(5, AppConfig.getInt("passwordReset.expirationMinutes", 15)));
                view(req, resp, "forgot-password.jsp");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/reset-password".equals(req.getServletPath())) {
                resetPassword(req, resp);
            } else {
                requestReset(req, resp);
            }
        } catch (Exception e) {
            req.setAttribute("error", "Không thể xử lý yêu cầu: " + e.getMessage());
            view(req, resp, "/reset-password".equals(req.getServletPath()) ? "reset-password.jsp" : "forgot-password.jsp");
        }
    }

    private void requestReset(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("error", "Email không đúng định dạng.");
            view(req, resp, "forgot-password.jsp");
            return;
        }

        Map<String, Object> account = accountDAO.findByEmail(email);
        int expiresMinutes = Math.max(5, AppConfig.getInt("passwordReset.expirationMinutes", 15));
        String token = accountDAO.createCustomerPasswordResetToken(email, expiresMinutes);
        if (token != null && account != null && "CUSTOMER".equals(String.valueOf(account.get("vaiTro")))) {
            String link = resolveResetBaseUrl(req) + "/reset-password?token=" + token;
            try {
                boolean sent = MailUtil.sendPasswordReset(
                        email, String.valueOf(account.get("hoTen")), link, expiresMinutes);
                if (!sent && AppConfig.getBoolean("app.devMode", true)) {
                    req.setAttribute("devResetLink", link);
                    req.setAttribute("mailNotice", "SMTP chưa được cấu hình nên đang dùng liên kết demo.");
                }
            } catch (Exception mailError) {
                getServletContext().log("Không gửi được email reset mật khẩu cho CUSTOMER " + email, mailError);
                if (AppConfig.getBoolean("app.devMode", true)) {
                    req.setAttribute("devResetLink", link);
                    req.setAttribute("mailNotice", "Gmail SMTP chưa gửi được. Bạn vẫn có thể thử luồng bằng liên kết demo bên dưới.");
                }
            }
        }
        req.setAttribute("passwordResetMinutes", expiresMinutes);
        req.setAttribute("success", "Nếu email thuộc tài khoản khách hàng đang hoạt động, Celine Closet đã gửi liên kết đặt lại mật khẩu.");
        view(req, resp, "forgot-password.jsp");
    }

    private void resetPassword(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String token = req.getParameter("token");
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");
        req.setAttribute("token", token);
        if (!ValidationUtil.isValidPassword(password)) {
            req.setAttribute("validReset", accountDAO.validCustomerPasswordReset(token) != null);
            req.setAttribute("error", ValidationUtil.passwordRuleMessage());
            view(req, resp, "reset-password.jsp");
            return;
        }
        if (!password.equals(confirm)) {
            req.setAttribute("validReset", accountDAO.validCustomerPasswordReset(token) != null);
            req.setAttribute("error", "Mật khẩu nhập lại không khớp.");
            view(req, resp, "reset-password.jsp");
            return;
        }
        if (!accountDAO.resetCustomerPassword(token, PasswordUtil.hash(password))) {
            req.setAttribute("validReset", false);
            req.setAttribute("error", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            view(req, resp, "reset-password.jsp");
            return;
        }
        req.setAttribute("success", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập.");
        view(req, resp, "login.jsp");
    }
    /**
     * Tự tạo base URL cho link trong email. Khi mở website qua Cloudflare Tunnel,
     * X-Forwarded-Proto / X-Forwarded-Host giúp link email trỏ ngược đúng tunnel.
     * Có thể ép URL cố định bằng passwordReset.baseUrl trong app.properties.
     */
    private String resolveResetBaseUrl(HttpServletRequest req) {
        String configured = AppConfig.get("passwordReset.baseUrl");
        if (!configured.isBlank() && !"auto".equalsIgnoreCase(configured)) {
            return configured.replaceAll("/+$", "");
        }

        String proto = req.getHeader("X-Forwarded-Proto");
        if (proto == null || proto.isBlank()) proto = req.getScheme();
        String host = req.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) host = req.getHeader("Host");
        if (host == null || host.isBlank()) {
            host = req.getServerName() + (req.getServerPort() > 0 ? ":" + req.getServerPort() : "");
        }
        return proto + "://" + host + req.getContextPath();
    }

}
