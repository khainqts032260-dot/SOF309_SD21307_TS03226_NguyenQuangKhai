package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.WishlistDAO;

import java.io.IOException;
import java.util.Map;

@WebServlet("/wishlist")
public class WishlistController extends BaseController {
    private final WishlistDAO dao = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, Object> user = auth(req);
        if (user == null || !"CUSTOMER".equals(String.valueOf(user.get("vaiTro")))) {
            resp.sendRedirect(req.getContextPath() + "/login?next=wishlist");
            return;
        }
        try {
            req.setAttribute("products", dao.products(authId(req)));
            view(req, resp, "wishlist.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        Map<String, Object> user = auth(req);
        if (user == null || !"CUSTOMER".equals(String.valueOf(user.get("vaiTro")))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"loginRequired\":true}");
            return;
        }
        try {
            int productId = Integer.parseInt(req.getParameter("productId"));
            boolean active;
            if ("remove".equals(req.getParameter("action"))) {
                dao.remove(authId(req), productId);
                active = false;
            } else {
                active = dao.toggle(authId(req), productId);
            }
            int count = dao.count(authId(req));
            resp.getWriter().write("{\"active\":" + active + ",\"count\":" + count + "}");
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Mã sản phẩm không hợp lệ\"}");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
