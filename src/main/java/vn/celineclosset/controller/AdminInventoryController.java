package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.InventoryDAO;

import java.io.IOException;

@WebServlet("/admin/inventory")
public class AdminInventoryController extends BaseController {
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private boolean requireInventoryRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireBackOffice(req, resp)) return false;
        String role = currentRole(req);
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) {
            resp.sendRedirect(adminStartUrl(req));
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireInventoryRole(req, resp)) return;
        try {
            req.setAttribute("pageTitle", "Quản lý kho hàng | Celine Closet");
            req.setAttribute("inventory", inventoryDAO.inventory(req.getParameter("q"), req.getParameter("stock")));
            view(req, resp, "admin/inventory.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireInventoryRole(req, resp)) return;
        try {
            int productId = Integer.parseInt(req.getParameter("maSP"));
            int quantity = Integer.parseInt(req.getParameter("soLuongNhap"));
            inventoryDAO.importStock(productId, quantity, authId(req), req.getParameter("ghiChu"), req.getParameter("soBienLai"), req.getParameter("nhaCungCap"), req.getParameter("xuatXu"));
            req.getSession().setAttribute("inventorySuccess", "Đã nhập thêm " + quantity + " sản phẩm vào kho.");
        } catch (Exception e) {
            req.getSession().setAttribute("inventoryError", e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/inventory");
    }
}
