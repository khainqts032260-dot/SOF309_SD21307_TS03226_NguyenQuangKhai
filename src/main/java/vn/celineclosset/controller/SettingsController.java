package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.util.FileUploadUtil;
import vn.celineclosset.util.PasswordUtil;
import vn.celineclosset.util.ValidationUtil;

import java.io.IOException;
import java.util.Map;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
@WebServlet("/settings")
public class SettingsController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        try {
            req.setAttribute("profile", accountDAO.accountById(authId(req)));
            view(req, resp, "settings.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        try {
            if ("password".equals(req.getParameter("action"))) {
                updatePassword(req, resp);
            } else {
                updateProfile(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void updatePassword(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        if (!ValidationUtil.isValidPassword(newPassword)) {
            resp.sendRedirect(req.getContextPath() + "/settings?error=passwordRule");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            resp.sendRedirect(req.getContextPath() + "/settings?error=password");
            return;
        }

        accountDAO.updatePassword(authId(req), PasswordUtil.hash(newPassword));
        resp.sendRedirect(req.getContextPath() + "/settings?success=password");
    }

    private void updateProfile(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        if (!ValidationUtil.isValidEmail(email)) {
            resp.sendRedirect(req.getContextPath() + "/settings?error=emailFormat");
            return;
        }
        if (accountDAO.emailExistsForOtherAccount(email, String.valueOf(authId(req)))) {
            resp.sendRedirect(req.getContextPath() + "/settings?error=email");
            return;
        }

        String avatarPath = FileUploadUtil.uploadImage(req, "avatarFile", "avatar", "/assets/uploads/avatars");
        accountDAO.updateProfile(authId(req), req.getParameter("hoTen"), email,
                req.getParameter("soDienThoai"), req.getParameter("diaChiMacDinh"), avatarPath);

        Map<String, Object> updated = accountDAO.accountById(authId(req));
        req.getSession().setAttribute("auth", updated);
        resp.sendRedirect(req.getContextPath() + "/settings?success=profile");
    }
}
