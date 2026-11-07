package vn.celineclosset.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.PaymentDAO;
import vn.celineclosset.payment.PaymentResult;
import vn.celineclosset.payment.SePayWebhookPayload;
import vn.celineclosset.util.AppConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Endpoint nhận giao dịch từ SePay.
 *
 * SePay chỉ xem webhook thành công khi server trả HTTP 200/201 và body chính xác
 * {"success":true}. Các trường hợp không ghép được đơn vẫn được lưu để đối soát,
 * nhưng không trả 400 để tránh SePay gửi lại liên tục.
 */
@WebServlet("/webhook/sepay")
public class SePayWebhookController extends HttpServlet {
    private static final Gson GSON = new Gson();
    private static final String SUCCESS_JSON = "{\"success\":true}";
    private static final String FAILED_JSON = "{\"success\":false}";
    private final PaymentDAO paymentDAO = new PaymentDAO();

    /** Cho phép mở URL bằng trình duyệt để kiểm tra endpoint đang hoạt động. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"success\":true,\"message\":\"SePay webhook endpoint is running. Use POST for transactions.\"}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        if (!AppConfig.getBoolean("sepay.webhookEnabled", false)) {
            writeFailure(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        String configuredKey = AppConfig.get("sepay.webhookApiKey").trim();
        String authorization = req.getHeader("Authorization");
        if (configuredKey.isBlank() || !matchesApiKey(configuredKey, authorization)) {
            writeFailure(resp, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final SePayWebhookPayload payload;
        try {
            payload = GSON.fromJson(req.getReader(), SePayWebhookPayload.class);
        } catch (JsonSyntaxException | IllegalStateException e) {
            writeFailure(resp, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        /*
         * Nút "Gửi thử" của SePay có thể gửi payload mẫu không có ID giao dịch thật.
         * Chỉ cần xác nhận endpoint đã nhận được request; không ghi nhận thanh toán.
         */
        if (payload == null || payload.getId() <= 0) {
            writeSuccess(resp);
            return;
        }

        try {
            PaymentResult result = paymentDAO.processSePayWebhook(
                    payload,
                    AppConfig.get("shop.bankAccount"),
                    AppConfig.get("payment.codePrefix", "DH")
            );

            // Ghi log nội bộ để dễ kiểm tra, nhưng phản hồi cho SePay luôn đúng hợp đồng.
            System.out.printf(
                    "[SePay] transaction=%d accepted=%s duplicate=%s orderId=%s status=%s message=%s%n",
                    payload.getId(), result.accepted(), result.duplicate(), result.orderId(),
                    result.paymentStatus(), result.message()
            );
            writeSuccess(resp);
        } catch (Exception e) {
            // Lỗi kỹ thuật cần trả 500 để SePay retry tự động.
            e.printStackTrace();
            writeFailure(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean matchesApiKey(String configuredKey, String authorization) {
        if (authorization == null) return false;
        String value = authorization.trim();
        String expected = "Apikey " + configuredKey;
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void writeSuccess(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(SUCCESS_JSON);
    }

    private void writeFailure(HttpServletResponse resp, int status) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(FAILED_JSON);
    }
}
