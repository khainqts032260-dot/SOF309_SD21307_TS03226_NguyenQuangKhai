package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.DashboardDAO;
import vn.celineclosset.dao.OrderDAO;

import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardController extends BaseController {
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            req.setAttribute("stats", dashboardDAO.stats());
            req.setAttribute("revenueLast7Days", dashboardDAO.revenueLast7Days());
            req.setAttribute("orderStatusStats", dashboardDAO.orderStatusStats());
            req.setAttribute("paymentStatusStats", dashboardDAO.paymentStatusStats());
            req.setAttribute("orders", orderDAO.allOrders());
            view(req, resp, "admin/dashboard.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
