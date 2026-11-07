package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/carts")
public class AdminCartController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) {
            return;
        }
        try {
            req.setAttribute("carts", cartDAO.allCarts(req.getParameter("q")));
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                int cartId = Integer.parseInt(id);
                req.setAttribute("cart", cartDAO.cartById(cartId));
                req.setAttribute("items", cartDAO.cartItemsByCartId(cartId));
            }
            view(req, resp, "admin/carts.jsp");
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
            String action = req.getParameter("action");
            String cartId = req.getParameter("maGH");
            if ("update".equals(action)) {
                cartDAO.updateCartItemAdmin(Integer.parseInt(req.getParameter("maCTGH")),
                        Integer.parseInt(req.getParameter("quantity")));
            } else if ("delete".equals(action)) {
                cartDAO.removeCartItemAdmin(Integer.parseInt(req.getParameter("maCTGH")));
            } else if ("clear".equals(action)) {
                cartDAO.clearCartAdmin(Integer.parseInt(cartId));
            } else if ("status".equals(action)) {
                cartDAO.toggleCartStatus(Integer.parseInt(cartId), Integer.parseInt(req.getParameter("status")));
            }
            String query = cartId == null || cartId.isBlank() ? "" : "?id=" + cartId;
            resp.sendRedirect(req.getContextPath() + "/admin/carts" + query);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
