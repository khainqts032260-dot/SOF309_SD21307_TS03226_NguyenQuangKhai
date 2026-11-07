package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.OrderDAO;
import vn.celineclosset.util.PasswordUtil;
import vn.celineclosset.util.ValidationUtil;

import java.io.IOException;

@WebServlet("/admin/customers")
public class AdminCustomerController extends BaseController {
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            req.setAttribute("customers", accountDAO.customers(req.getParameter("q")));
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                int customerId = Integer.parseInt(id);
                req.setAttribute("customer", accountDAO.customer(customerId));
                req.setAttribute("orders", orderDAO.customerOrders(customerId));
            }
            view(req, resp, "admin/customers.jsp");
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
                accountDAO.toggleCustomer(Integer.parseInt(req.getParameter("id")),
                        Integer.parseInt(req.getParameter("status")));
                resp.sendRedirect(req.getContextPath() + "/admin/customers?success=status");
                return;
            }

            String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
            String password = req.getParameter("password");
            if (!ValidationUtil.isValidEmail(email)) {
                resp.sendRedirect(req.getContextPath() + "/admin/customers?error=emailFormat");
                return;
            }
            if (accountDAO.emailExists(email)) {
                resp.sendRedirect(req.getContextPath() + "/admin/customers?error=duplicateEmail");
                return;
            }
            if (!ValidationUtil.isValidPassword(password)) {
                resp.sendRedirect(req.getContextPath() + "/admin/customers?error=passwordRule");
                return;
            }

            accountDAO.saveCustomer(req.getParameter("hoTen"), email,
                    PasswordUtil.hash(password), req.getParameter("soDienThoai"));
            resp.sendRedirect(req.getContextPath() + "/admin/customers?success=add");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
