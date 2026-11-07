package vn.celineclosset.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.PaymentDAO;
import vn.celineclosset.util.AppConfig;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/payment-status")
public class PaymentStatusApiController extends BaseController {
    private static final Gson GSON = new Gson();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (auth(req) == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Bạn cần đăng nhập.\"}");
            return;
        }
        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            int expirationMinutes = AppConfig.getInt("payment.expirationMinutes", 10);
            paymentDAO.expirePendingBankPayments(expirationMinutes);
            Map<String, Object> payment = paymentDAO.paymentForCustomer(authId(req), orderId);
            if (payment == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"success\":false,\"message\":\"Không tìm thấy thanh toán.\"}");
                return;
            }
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("success", true);
            json.put("orderId", orderId);
            json.put("status", payment.get("trangThai"));
            json.put("orderStatus", payment.get("trangThaiDon"));
            json.put("reconciliationStatus", payment.get("trangThaiDoiSoat"));
            json.put("expectedAmount", payment.get("soTien"));
            json.put("receivedAmount", payment.get("soTienDaNhan"));
            json.put("paymentCode", payment.get("noiDungChuyenKhoan"));
            json.put("paidAt", payment.get("ngayThanhToan"));
            json.put("note", payment.get("ghiChuDoiSoat"));
            json.put("expirationMinutes", expirationMinutes);
            json.put("secondsRemaining",
                    paymentDAO.paymentSecondsRemaining(authId(req), orderId, expirationMinutes));
            resp.getWriter().write(GSON.toJson(json));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Mã đơn không hợp lệ.\"}");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
