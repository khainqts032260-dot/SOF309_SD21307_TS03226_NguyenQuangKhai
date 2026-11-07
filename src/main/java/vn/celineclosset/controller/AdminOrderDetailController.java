package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.OrderDAO;
import java.io.IOException;
import java.util.Map;

/** Trang chi tiết đơn riêng, tránh cuộn xuống cuối danh sách đơn. */
@WebServlet("/admin/order-detail")
public class AdminOrderDetailController extends BaseController {
    private final OrderDAO orderDAO = new OrderDAO();
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOrderRole(req, resp)) return;
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Map<String,Object> selected = orderDAO.orderByIdForUser(id, currentRole(req), authId(req));
            if (selected == null) { resp.sendRedirect(req.getContextPath()+"/admin/orders?error=permission"); return; }
            req.setAttribute("selectedOrder", selected);
            req.setAttribute("items", orderDAO.orderItems(id));
            req.setAttribute("trackingHistory", orderDAO.trackingHistory(id));
            req.setAttribute("nextOrderStatus", orderDAO.nextAllowedStatus(String.valueOf(selected.get("trangThai")), currentRole(req)));
            req.setAttribute("detailOnly", true);
            view(req, resp, "admin/orders.jsp");
        } catch (Exception e) { throw new ServletException(e); }
    }
}
