package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.DeliveryFailureDAO;
import vn.celineclosset.util.FileUploadUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.Map;

/** Màn hình shipper nộp minh chứng giao không thành công và ADMIN duyệt. */
@WebServlet("/admin/delivery-support")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 7 * 1024 * 1024)
public class DeliveryFailureController extends BaseController {
    private final DeliveryFailureDAO failureDAO = new DeliveryFailureDAO();

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
            req.setAttribute("failureRequests", failureDAO.requests(role, accountId));
            if ("DELIVERY".equals(role)) {
                req.setAttribute("deliveryOrders", failureDAO.eligibleOrders(accountId));
            }

            Map<String, Object> selected = null;
            String requestId = req.getParameter("id");
            String orderId = req.getParameter("orderId");
            if (requestId != null && !requestId.isBlank()) {
                selected = failureDAO.requestById(Integer.parseInt(requestId), role, accountId);
            } else if (orderId != null && !orderId.isBlank()) {
                selected = failureDAO.requestByOrder(Integer.parseInt(orderId), role, accountId);
                if (selected == null && "DELIVERY".equals(role)) {
                    req.setAttribute("selectedOrderId", Integer.parseInt(orderId));
                }
            }
            if (selected != null) {
                req.setAttribute("selectedFailure", selected);
                req.setAttribute("failureAttempts", failureDAO.attempts(((Number) selected.get("maYCGTB")).intValue()));
                req.setAttribute("selectedOrderId", ((Number) selected.get("maDH")).intValue());
            }
            view(req, resp, "admin/delivery-support.jsp");
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireBackOffice(req, resp)) return;
        String role = currentRole(req);
        try {
            String action = clean(req.getParameter("action"));
            if ("addAttempt".equals(action)) {
                requireDelivery(role);
                int orderId = Integer.parseInt(req.getParameter("maDH"));
                String image = FileUploadUtil.uploadImage(req, "callEvidence", "delivery-call-" + orderId,
                        "/assets/uploads/delivery-evidence");
                failureDAO.addAttempt(orderId, authId(req), LocalDate.parse(req.getParameter("ngayGiao")),
                        Integer.parseInt(req.getParameter("soLanGoi")), image, req.getParameter("ghiChu"));
                resp.sendRedirect(req.getContextPath() + "/admin/delivery-support?orderId=" + orderId + "&saved=attempt");
                return;
            }
            if ("submit".equals(action)) {
                requireDelivery(role);
                int orderId = Integer.parseInt(req.getParameter("maDH"));
                failureDAO.submit(orderId, authId(req), req.getParameter("lyDo"));
                resp.sendRedirect(req.getContextPath() + "/admin/delivery-support?orderId=" + orderId + "&saved=submitted");
                return;
            }
            if ("approve".equals(action) || "reject".equals(action)) {
                if (!"ADMIN".equals(role)) throw new IllegalAccessException("Chỉ ADMIN được duyệt hồ sơ.");
                int requestId = Integer.parseInt(req.getParameter("maYCGTB"));
                failureDAO.review(requestId, authId(req), "approve".equals(action), req.getParameter("ghiChuAdmin"));
                resp.sendRedirect(req.getContextPath() + "/admin/delivery-support?id=" + requestId
                        + "&saved=" + ("approve".equals(action) ? "approved" : "rejected"));
                return;
            }
            throw new IllegalArgumentException("Thao tác không hợp lệ.");
        } catch (IllegalAccessException exception) {
            resp.sendRedirect(adminStartUrl(req));
        } catch (IllegalStateException | IllegalArgumentException | SQLException exception) {
            String orderId = clean(req.getParameter("maDH"));
            String requestId = clean(req.getParameter("maYCGTB"));
            String query = !orderId.isBlank() ? "orderId=" + orderId : (!requestId.isBlank() ? "id=" + requestId : "");
            req.getSession().setAttribute("deliverySupportError", exception.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/delivery-support?" + query + "&error=1");
        } catch (Exception exception) {
            throw new ServletException(exception);
        }
    }

    private void requireDelivery(String role) throws IllegalAccessException {
        if (!"DELIVERY".equals(role)) throw new IllegalAccessException("Chỉ shipper được ghi nhận lần giao.");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
