package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.OrderDAO;
import vn.celineclosset.dao.NotificationDAO;
import vn.celineclosset.dao.FeedbackDAO;
import vn.celineclosset.dao.ReturnRequestDAO;

import java.io.IOException;
import java.util.Map;

@WebServlet("/orders")
public class CustomerOrderController extends BaseController {
    private final OrderDAO orderDAO = new OrderDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ReturnRequestDAO returnDAO = new ReturnRequestDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        try {
            int customerId = authId(req);
            req.setAttribute("orders", orderDAO.myOrders(customerId));
            var notifications = notificationDAO.unread(customerId, 5);
            req.setAttribute("orderNotifications", notifications);
            notificationDAO.markRead(customerId, notifications);
            String orderId = req.getParameter("id");
            if (orderId != null && !orderId.isBlank()) {
                int id = Integer.parseInt(orderId);
                Map<String, Object> selectedOrder = orderDAO.myOrderById(customerId, id);
                if (selectedOrder != null) {
                    req.setAttribute("selectedOrder", selectedOrder);
                    feedbackDAO.ensureReviewSchema();
                    req.setAttribute("items", orderDAO.orderItemsForCustomer(id, customerId));
                    req.setAttribute("trackingHistory", orderDAO.trackingHistory(id));
                    Map<String, Object> returnRequest = returnDAO.returnForCustomer(id, customerId);
                    req.setAttribute("returnRequest", returnRequest);
                    req.setAttribute("canRequestReturn", returnDAO.canRequest(id, customerId));
                    req.setAttribute("returnDaysRemaining", returnDAO.daysRemaining(id, customerId));
                    if (returnRequest != null) {
                        req.setAttribute("returnImages", returnDAO.images(((Number) returnRequest.get("maYCTH")).intValue()));
                    }
                }
            }
            view(req, resp, "orders.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        try {
            int orderId = Integer.parseInt(req.getParameter("maDH"));
            String action = req.getParameter("action");
            if ("received".equals(action)) {
                orderDAO.confirmReceivedByCustomer(authId(req), orderId);
                resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId + "&received=1");
                return;
            }
            String reason = req.getParameter("cancelReason");
            String other = req.getParameter("otherReason");
            orderDAO.cancelOrderByCustomer(authId(req), orderId, reason, other);
            resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId + "&cancelled=1");
        } catch (IllegalStateException e) {
            req.getSession().setAttribute("customerOrderError", e.getMessage());
            String orderId = req.getParameter("maDH");
            resp.sendRedirect(req.getContextPath() + "/orders" + (orderId == null ? "" : "?id=" + orderId + "&error=action"));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
