package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.ReturnRequestDAO;

import java.io.IOException;
import java.util.Map;

/** ADMIN duyệt/hoàn tiền; DELIVERY cập nhật chặng nhận hàng và đến bưu điện. */
@WebServlet("/admin/returns")
public class AdminReturnController extends BaseController {
    private final ReturnRequestDAO returnDAO = new ReturnRequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireBackOffice(req, resp)) return;
        String role = currentRole(req);
        if (!("ADMIN".equals(role) || "DELIVERY".equals(role))) {
            resp.sendRedirect(adminStartUrl(req));
            return;
        }
        try {
            int accountId = authId(req);
            req.setAttribute("returnRequests", returnDAO.requests(role, accountId));
            if ("ADMIN".equals(role)) req.setAttribute("deliveryAccounts", returnDAO.deliveryAccounts());
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                Map<String, Object> selected = returnDAO.requestById(Integer.parseInt(id), role, accountId);
                if (selected != null) {
                    req.setAttribute("selectedReturn", selected);
                    req.setAttribute("returnImages", returnDAO.images(((Number) selected.get("maYCTH")).intValue()));
                }
            }
            view(req, resp, "admin/returns.jsp");
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireBackOffice(req, resp)) return;
        String role = currentRole(req);
        int requestId = parseInt(req.getParameter("maYCTH"));
        try {
            String action = clean(req.getParameter("action"));
            if ("assign".equals(action)) {
                if (!"ADMIN".equals(role)) throw new IllegalAccessException("Chỉ ADMIN được phân công shipper.");
                returnDAO.assignDelivery(requestId, authId(req), parseInt(req.getParameter("deliveryId")));
            } else {
                returnDAO.updateStatus(requestId, authId(req), role, action, req.getParameter("ghiChu"));
            }
            resp.sendRedirect(req.getContextPath() + "/admin/returns?id=" + requestId + "&saved=1");
        } catch (IllegalArgumentException | IllegalStateException | IllegalAccessException exception) {
            req.getSession().setAttribute("adminReturnError", exception.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/returns?id=" + requestId + "&error=1");
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value == null ? "0" : value.trim()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Mã yêu cầu không hợp lệ."); }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
