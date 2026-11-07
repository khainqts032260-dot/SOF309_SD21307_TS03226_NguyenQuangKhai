package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.OrderDAO;

import java.io.IOException;
import java.util.Map;
import java.sql.SQLException;

@WebServlet("/admin/order-tracking")
public class AdminOrderTrackingController extends BaseController {
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOrderRole(req, resp)) return;
        try {
            String role = currentRole(req);
            int accountId = authId(req);
            req.setAttribute("orders", orderDAO.ordersForTracking(req.getParameter("q"), req.getParameter("status"), role, accountId));
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                int orderId = Integer.parseInt(id);
                Map<String, Object> selected = orderDAO.orderByIdForUser(orderId, role, accountId);
                if (selected != null) {
                    req.setAttribute("selectedOrder", selected);
                    req.setAttribute("items", orderDAO.orderItems(orderId));
                    req.setAttribute("trackingHistory", orderDAO.trackingHistory(orderId));
                    req.setAttribute("nextOrderStatus", orderDAO.nextAllowedStatus(String.valueOf(selected.get("trangThai")), role));
                }
            }
            view(req, resp, "admin/order-tracking.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOrderRole(req, resp)) return;
        try {
            int orderId = Integer.parseInt(req.getParameter("maDH"));
            String role = currentRole(req);
            int accountId = authId(req);
            if (("DELIVERY".equals(role) && !orderDAO.staffOwnsOrder(orderId, accountId))
                    || ("STAFF".equals(role) && !orderDAO.processingStaffOwnsOrder(orderId, accountId))) {
                resp.sendRedirect(req.getContextPath() + "/admin/order-tracking?error=permission");
                return;
            }
            String action = req.getParameter("action");
            if (action == null || "orderStatus".equals(action)) {
                orderDAO.updateOrderStatus(orderId, req.getParameter("trangThai"), accountId);
                orderDAO.addDeliveryNote(orderId, accountId, req.getParameter("deliveryNote"));
            } else if ("assign".equals(action)) {
                if ("DELIVERY".equals(role)) throw new IllegalAccessException("Nhân viên không được phân công lại đơn.");
                orderDAO.assignStaff(orderId, Integer.parseInt(req.getParameter("staffId")));
            } else if ("payment".equals(action)) {
                if ("DELIVERY".equals(role)) throw new IllegalAccessException("Nhân viên giao hàng không được xác nhận thanh toán.");
                orderDAO.updatePaymentStatus(orderId, req.getParameter("paymentStatus"), accountId);
            } else if ("cancel".equals(action)) {
                if ("DELIVERY".equals(role)) throw new IllegalAccessException("Nhân viên giao hàng không được hủy đơn.");
                orderDAO.cancelOrder(orderId, req.getParameter("cancelReason"));
            } else if ("error".equals(action)) {
                if ("DELIVERY".equals(role)) throw new IllegalAccessException("Nhân viên giao hàng không được báo lỗi thanh toán.");
                orderDAO.reportOrderError(orderId, req.getParameter("errorNote"));
            }
            resp.sendRedirect(req.getContextPath() + "/admin/order-tracking?id=" + orderId + "&saved=1");
        } catch (IllegalAccessException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/order-tracking?error=permission");
        } catch (IllegalStateException | SQLException e) {
            req.getSession().setAttribute("orderActionError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/order-tracking?id=" + req.getParameter("maDH") + "&error=action");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
