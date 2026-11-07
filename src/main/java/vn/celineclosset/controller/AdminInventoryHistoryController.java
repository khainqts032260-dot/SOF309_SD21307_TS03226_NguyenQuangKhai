package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.InventoryDAO;

import java.io.IOException;

/** Trang riêng để xem và lọc lịch sử nhập kho, không làm dài trang tồn kho. */
@WebServlet("/admin/inventory-history")
public class AdminInventoryHistoryController extends BaseController {
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private boolean requireInventoryRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireBackOffice(req, resp)) return false;
        String role = currentRole(req);
        if (!("ADMIN".equals(role) || "STAFF".equals(role))) {
            resp.sendRedirect(adminStartUrl(req));
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireInventoryRole(req, resp)) return;
        try {
            req.setAttribute("pageTitle", "Lịch sử nhập kho | Celine Closet");
            req.setAttribute("history", inventoryDAO.importHistory(
                    req.getParameter("q"), req.getParameter("staffId"),
                    req.getParameter("from"), req.getParameter("to"), 500));
            req.setAttribute("inventoryStaff", inventoryDAO.inventoryStaffAccounts());
            view(req, resp, "admin/inventory-history.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
