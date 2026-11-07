package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.util.PasswordUtil;
import vn.celineclosset.util.ValidationUtil;

import java.io.IOException;

@WebServlet("/admin/accounts")
public class AdminAccountController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            req.setAttribute("accounts", accountDAO.staff(false));
            String editId = req.getParameter("edit");
            if (editId != null && !editId.isBlank()) {
                req.setAttribute("editAccount", accountDAO.staffById(Integer.parseInt(editId), false));
            }
            view(req, resp, "admin/accounts.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            if ("status".equals(req.getParameter("action"))) {
                accountDAO.toggleStaff(Integer.parseInt(req.getParameter("id")),
                        Integer.parseInt(req.getParameter("status")), false);
                resp.sendRedirect(req.getContextPath() + "/admin/accounts?success=status");
                return;
            }

            saveAccount(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void saveAccount(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String staffId = req.getParameter("maTK");
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");
        boolean creating = staffId == null || staffId.isBlank();

        if (!ValidationUtil.isValidEmail(email)) {
            redirectError(req, resp, "emailFormat", staffId);
            return;
        }
        if (accountDAO.emailExistsForOtherAccount(email, staffId)) {
            redirectError(req, resp, "duplicateEmail", staffId);
            return;
        }
        if (creating && (password == null || password.isBlank())) {
            redirectError(req, resp, "missingPassword", staffId);
            return;
        }
        if (password != null && !password.isBlank() && !ValidationUtil.isValidPassword(password)) {
            redirectError(req, resp, "passwordRule", staffId);
            return;
        }

        String requestedRole = "DELIVERY".equals(req.getParameter("vaiTro")) ? "DELIVERY" : "STAFF";
        String hash = password == null || password.isBlank() ? null : PasswordUtil.hash(password);
        accountDAO.saveStaff(staffId, req.getParameter("hoTen"), email, hash,
                req.getParameter("soDienThoai"), requestedRole);
        resp.sendRedirect(req.getContextPath() + "/admin/accounts?success=" + (creating ? "add" : "edit"));
    }

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String error, String staffId)
            throws IOException {
        String editQuery = staffId == null || staffId.isBlank() ? "" : "&edit=" + staffId;
        resp.sendRedirect(req.getContextPath() + "/admin/accounts?error=" + error + editQuery);
    }
}
