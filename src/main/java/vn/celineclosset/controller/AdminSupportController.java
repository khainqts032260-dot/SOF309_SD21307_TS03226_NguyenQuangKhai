package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.SupportDAO;

import java.io.IOException;

@WebServlet("/admin/support")
public class AdminSupportController extends BaseController {
    private final SupportDAO supportDAO = new SupportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireBackOffice(req, resp)) return;
        if ("DELIVERY".equals(currentRole(req))) { resp.sendRedirect(adminStartUrl(req)); return; }
        try {
            String role = currentRole(req);
            int accountId = authId(req);
            if ("ADMIN".equals(role)) req.setAttribute("staffList", accountDAO.employeesByRole("STAFF"));

            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                int requestId = Integer.parseInt(id);
                supportDAO.markCustomerMessagesRead(requestId, role, accountId);
                req.setAttribute("selectedRequest", supportDAO.requestForBackOffice(requestId, role, accountId));
                req.setAttribute("messages", supportDAO.messages(requestId));
            }
            req.setAttribute("requests", supportDAO.backOfficeRequests(role, accountId, req.getParameter("status")));
            view(req, resp, "admin/support.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireBackOffice(req, resp)) return;
        if ("DELIVERY".equals(currentRole(req))) { resp.sendRedirect(adminStartUrl(req)); return; }
        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"));
        int requestId = 0;
        try {
            requestId = Integer.parseInt(req.getParameter("maYC"));
            String role = currentRole(req);
            String action = req.getParameter("action");

            if ("assign".equals(action)) {
                if (!"ADMIN".equals(role)) throw new IllegalAccessException("Bạn không có quyền phân công yêu cầu này.");
                supportDAO.assign(requestId, Integer.parseInt(req.getParameter("staffId")));
            } else {
                String message = req.getParameter("phanHoi");
                if (message == null || message.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vui lòng nhập nội dung phản hồi.");
                }
                supportDAO.reply(requestId, authId(req), role, message, req.getParameter("trangThai"));
            }

            if (ajax) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":true}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/admin/support?id=" + requestId + "&saved=1");
        } catch (Exception e) {
            if (ajax) {
                int status = (e instanceof IllegalAccessException) ? HttpServletResponse.SC_FORBIDDEN
                        : HttpServletResponse.SC_BAD_REQUEST;
                resp.setStatus(status);
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("application/json;charset=UTF-8");
                String message = e.getMessage() == null ? "Không gửi được phản hồi." : e.getMessage();
                resp.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}");
                return;
            }
            throw new ServletException(e);
        }
    }
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }

}
