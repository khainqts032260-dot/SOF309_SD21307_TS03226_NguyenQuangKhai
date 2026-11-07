package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.util.FileUploadUtil;

import java.io.IOException;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 8 * 1024 * 1024)
@WebServlet("/admin/profile")
public class StaffProfileController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireBackOffice(req, resp)) return;
        try {
            req.setAttribute("pageTitle", "Hồ sơ nhân viên | Celine Closet");
            req.setAttribute("profile", accountDAO.staffProfile(authId(req)));
            req.setAttribute("processedOrders", accountDAO.processedOrders(authId(req)));
            view(req, resp, "admin/staff-profile.jsp");
        } catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireBackOffice(req, resp)) return;
        try {
            String avatar = FileUploadUtil.uploadImage(req, "avatar", "staff", "/assets/uploads/staff");
            accountDAO.updateProfile(authId(req), req.getParameter("hoTen"), req.getParameter("email"),
                    req.getParameter("soDienThoai"), req.getParameter("diaChiMacDinh"), avatar);
            req.getSession().setAttribute("auth", accountDAO.accountById(authId(req)));
            req.getSession().setAttribute("profileSuccess", "Đã cập nhật hồ sơ nhân viên.");
        } catch (Exception e) { req.getSession().setAttribute("profileError", e.getMessage()); }
        resp.sendRedirect(req.getContextPath() + "/admin/profile");
    }
}
