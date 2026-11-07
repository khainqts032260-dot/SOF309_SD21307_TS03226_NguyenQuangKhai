package vn.celineclosset.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.service.CustomerSupportService;

import java.io.IOException;
import java.util.Map;

/** Endpoint AJAX riêng cho chatbox để không reload giao diện và không lộ API key. */
@WebServlet("/api/support/chat")
public class SupportChatApiController extends BaseController {
    private final CustomerSupportService customerSupportService = new CustomerSupportService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> user = auth(req);
        if (user == null || !"CUSTOMER".equals(String.valueOf(user.get("vaiTro")))) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập để sử dụng chat.");
            return;
        }

        try {
            int customerId = ((Number) user.get("maTK")).intValue();
            String action = clean(req.getParameter("action"));
            CustomerSupportService.ChatResult result;

            if ("create".equals(action)) {
                result = customerSupportService.startConversation(
                        customerId,
                        String.valueOf(user.getOrDefault("hoTen", "Khách hàng")),
                        String.valueOf(user.getOrDefault("email", "")),
                        String.valueOf(user.getOrDefault("soDienThoai", "")),
                        clean(req.getParameter("chuDe")),
                        clean(req.getParameter("noiDung"))
                );
            } else if ("message".equals(action)) {
                int requestId = Integer.parseInt(clean(req.getParameter("maYC")));
                result = customerSupportService.sendMessage(
                        requestId, customerId, clean(req.getParameter("noiDung")));
            } else {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thao tác chat không hợp lệ.");
                return;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("success", true);
            payload.addProperty("requestId", result.requestId());
            payload.addProperty("reply", result.reply());
            payload.addProperty("humanAssigned", result.humanAssigned());
            payload.addProperty("staffName", result.staffName());
            payload.addProperty("aiUsed", result.aiUsed());
            payload.addProperty("state", result.state());
            payload.add("products", gson.toJsonTree(result.products()));
            resp.getWriter().write(gson.toJson(payload));
        } catch (NumberFormatException exception) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Mã cuộc trò chuyện không hợp lệ.");
        } catch (IllegalArgumentException exception) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (Exception exception) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Hệ thống chat đang bận. Vui lòng thử lại hoặc liên hệ nhân viên hỗ trợ.");
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject payload = new JsonObject();
        payload.addProperty("success", false);
        payload.addProperty("message", message == null ? "Không xử lý được yêu cầu." : message);
        resp.getWriter().write(gson.toJson(payload));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
