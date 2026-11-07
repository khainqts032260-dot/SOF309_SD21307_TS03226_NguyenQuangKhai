package vn.celineclosset.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/cart")
public class CartController extends BaseController {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireCustomer(req, resp)) {
            return;
        }
        try {
            req.setAttribute("items", cartDAO.cartItems(authId(req)));
            req.setAttribute("total", cartDAO.cartTotal(authId(req)));
            view(req, resp, "cart.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        boolean ajax = isAjax(req);

        if (ajax) {
            try {
                Map<String, Object> user = refreshAuthenticatedUser(req);
                if (user == null || !"CUSTOMER".equals(String.valueOf(user.get("vaiTro")))) {
                    writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                            Map.of("success", false, "loginRequired", true, "message", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ."));
                    return;
                }
            } catch (Exception e) {
                writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                        Map.of("success", false, "loginRequired", true, "message", "Phiên đăng nhập đã hết hạn."));
                return;
            }
        } else if (!requireCustomer(req, resp)) {
            return;
        }

        try {
            String action = req.getParameter("action");
            String redirectPath = "/cart";
            Integer addedItemId = null;

            if ("add".equals(action)) {
                String quantityValue = req.getParameter("quantity");
                int quantity = quantityValue == null || quantityValue.isBlank() ? 1 : Integer.parseInt(quantityValue);
                addedItemId = cartDAO.addCart(authId(req), Integer.parseInt(req.getParameter("productId")), quantity,
                        req.getParameter("selectedColor"), req.getParameter("selectedSize"));
                if ("1".equals(req.getParameter("buyNow"))) {
                    redirectPath = "/checkout?selectedItemId=" + addedItemId;
                }
            } else if ("update".equals(action)) {
                cartDAO.updateCart(authId(req), Integer.parseInt(req.getParameter("itemId")),
                        Integer.parseInt(req.getParameter("quantity")));
            } else if ("plus".equals(action)) {
                cartDAO.changeQuantity(authId(req), Integer.parseInt(req.getParameter("itemId")), 1);
            } else if ("minus".equals(action)) {
                cartDAO.changeQuantity(authId(req), Integer.parseInt(req.getParameter("itemId")), -1);
            } else if ("remove".equals(action)) {
                cartDAO.removeCartItem(authId(req), Integer.parseInt(req.getParameter("itemId")));
            }

            if (ajax) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("success", true);
                payload.put("cartCount", cartDAO.cartItemCount(authId(req)));
                payload.put("itemId", addedItemId);
                payload.put("message", "Đã thêm sản phẩm vào giỏ hàng.");
                if (!"/cart".equals(redirectPath)) payload.put("redirect", req.getContextPath() + redirectPath);
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }
            resp.sendRedirect(req.getContextPath() + redirectPath);
        } catch (Exception e) {
            if (ajax) {
                String message = e.getMessage();
                if (message == null || message.isBlank()) message = "Không thể cập nhật giỏ hàng. Vui lòng thử lại.";
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Map.of("success", false, "message", message));
                return;
            }
            throw new ServletException(e);
        }
    }

    private boolean isAjax(HttpServletRequest req) {
        return "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || "1".equals(req.getParameter("ajax"));
    }

    private void writeJson(HttpServletResponse resp, int status, Object payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(payload));
    }
}
