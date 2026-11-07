package vn.celineclosset.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.PaymentDAO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Khách chủ động bấm Hủy QR: rollback đơn tạm và quay lại checkout; đóng tab không gọi API này. */
@WebServlet("/api/payment-abandon")
public class PaymentAbandonApiController extends BaseController {
    private static final Gson GSON = new Gson();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (!requireCustomer(req, resp)) return;
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            PaymentDAO.CheckoutRollbackResult rollback =
                    paymentDAO.rollbackPendingBankCheckout(authId(req), orderId);
            result.put("success", true);
            result.put("cancelled", rollback.rolledBack());
            result.put("rolledBack", rollback.rolledBack());
            result.put("message", rollback.message());

            if (rollback.rolledBack()) {
                StringBuilder checkoutUrl = new StringBuilder(req.getContextPath())
                        .append("/checkout?payment=BANK&restoreCheckout=1&paymentCancelled=1");
                if (rollback.voucherCode() != null && !rollback.voucherCode().isBlank()) {
                    checkoutUrl.append("&voucherCode=")
                            .append(URLEncoder.encode(rollback.voucherCode(), StandardCharsets.UTF_8));
                }
                for (Integer cartItemId : rollback.restoredCartItemIds()) {
                    checkoutUrl.append("&selectedItemId=").append(cartItemId);
                }
                checkoutUrl.append("#payment-method");
                result.put("checkoutUrl", checkoutUrl.toString());
            }
            resp.getWriter().write(GSON.toJson(result));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Mã đơn không hợp lệ.");
            resp.getWriter().write(GSON.toJson(result));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
