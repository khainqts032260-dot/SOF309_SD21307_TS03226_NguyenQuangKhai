package vn.celineclosset.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.OrderDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/voucher-check")
public class VoucherCheckApiController extends BaseController {
    private final OrderDAO orderDAO = new OrderDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<>();
        if (auth(req) == null || !"CUSTOMER".equals(String.valueOf(auth(req).get("vaiTro")))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("valid", false);
            result.put("message", "Vui lòng đăng nhập để dùng voucher.");
            resp.getWriter().write(gson.toJson(result));
            return;
        }
        try {
            String code = req.getParameter("voucherCode");
            BigDecimal total = cartDAO.cartTotal(authId(req), req.getParameterValues("selectedItemId"));
            boolean valid = orderDAO.validVoucher(code, total, authId(req));
            BigDecimal discount = valid ? orderDAO.voucherDiscount(code, total, authId(req)) : BigDecimal.ZERO;
            result.put("valid", valid);
            result.put("total", total);
            result.put("discount", discount);
            result.put("payable", total.subtract(discount).max(BigDecimal.ZERO));
            result.put("code", code == null ? "" : code.trim().toUpperCase());
            result.put("message", code == null || code.isBlank() ? "" : valid
                    ? "Áp dụng voucher thành công." : "Mã không hợp lệ, hết hạn, hết lượt hoặc chưa đủ điều kiện đơn hàng.");
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
