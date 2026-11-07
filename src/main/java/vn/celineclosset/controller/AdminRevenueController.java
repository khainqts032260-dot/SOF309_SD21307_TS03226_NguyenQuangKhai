package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.RevenueReportDAO;

import java.io.IOException;

@WebServlet("/admin/revenue")
public class AdminRevenueController extends BaseController {
    private final RevenueReportDAO revenueReportDAO = new RevenueReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");
            req.setAttribute("summary", revenueReportDAO.summary(fromDate, toDate));
            req.setAttribute("revenueByDate", revenueReportDAO.revenueByDate(fromDate, toDate));
            req.setAttribute("revenueByCategory", revenueReportDAO.revenueByCategory(fromDate, toDate));
            req.setAttribute("topProducts", revenueReportDAO.topProducts(fromDate, toDate));
            req.setAttribute("paymentStatusStats", revenueReportDAO.paymentStatusStats(fromDate, toDate));
            req.setAttribute("orderStatusStats", revenueReportDAO.orderStatusStats(fromDate, toDate));
            req.setAttribute("recentOrders", revenueReportDAO.recentPaidOrders(fromDate, toDate));
            view(req, resp, "admin/revenue.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
