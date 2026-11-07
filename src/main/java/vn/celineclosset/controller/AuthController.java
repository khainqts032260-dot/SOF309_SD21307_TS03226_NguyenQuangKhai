package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.util.PasswordUtil;
import vn.celineclosset.util.ValidationUtil;

import java.io.IOException;
import java.util.Map;

@WebServlet({"/login", "/register", "/logout"})
public class AuthController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/home");
        } else if ("/register".equals(path)) {
            view(req, resp, "register.jsp");
        } else {
            view(req, resp, "login.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/register".equals(req.getServletPath())) {
                register(req, resp);
            } else {
                login(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");

        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
            view(req, resp, "login.jsp");
            return;
        }
        if (!accountDAO.emailExists(email)) {
            req.setAttribute("error", "Email này chưa có trong hệ thống.");
            view(req, resp, "login.jsp");
            return;
        }

        Map<String, Object> user = accountDAO.login(email, PasswordUtil.hash(password));
        if (user == null) {
            req.setAttribute("error", "Mật khẩu không đúng hoặc tài khoản đang bị khóa.");
            view(req, resp, "login.jsp");
            return;
        }

        req.getSession().setAttribute("auth", user);
        if (isBackOffice(user)) {
            resp.sendRedirect(req.getContextPath() + adminStartPath(user));
        } else {
            String next = req.getParameter("next");
            String target = "wishlist".equals(next) ? "/wishlist" : "/home";
            resp.sendRedirect(req.getContextPath() + target);
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("error", "Email không đúng định dạng. Ví dụ: ten@gmail.com.");
            view(req, resp, "register.jsp");
            return;
        }
        if (accountDAO.emailExists(email)) {
            req.setAttribute("error", "Email đã tồn tại.");
            view(req, resp, "register.jsp");
            return;
        }
        String phone = req.getParameter("soDienThoai");
        if (!ValidationUtil.isValidVietnamPhone(phone)) {
            req.setAttribute("error", ValidationUtil.phoneRuleMessage());
            view(req, resp, "register.jsp");
            return;
        }
        if (!ValidationUtil.isValidPassword(password)) {
            req.setAttribute("error", ValidationUtil.passwordRuleMessage());
            view(req, resp, "register.jsp");
            return;
        }
        if (!password.equals(confirmPassword)) {
            req.setAttribute("error", "Mật khẩu nhập lại không khớp.");
            view(req, resp, "register.jsp");
            return;
        }

        accountDAO.register(req.getParameter("hoTen"), email,
                PasswordUtil.hash(password), phone.trim());
        req.setAttribute("success", "Đăng ký thành công, bạn có thể đăng nhập.");
        view(req, resp, "login.jsp");
    }
}
