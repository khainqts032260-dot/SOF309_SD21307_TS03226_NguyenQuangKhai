package vn.celineclosset.service;

import vn.celineclosset.dao.SupportDAO;
import vn.celineclosset.dao.ProductDAO;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Điều phối chat AI và chuyển sang STAFF khi cần hoặc khi OpenRouter gặp lỗi. */
public final class CustomerSupportService {
    public record ChatResult(int requestId, String reply, boolean humanAssigned,
                             String staffName, boolean aiUsed, String state,
                             List<Map<String, Object>> products) {
    }

    private static final String HUMAN_MARKER = "[[HUMAN_SUPPORT]]";
    private final SupportDAO supportDAO;
    private final OpenRouterClient openRouterClient;
    private final ProductDAO productDAO = new ProductDAO();

    public CustomerSupportService() {
        this(new SupportDAO(), new OpenRouterClient());
    }

    CustomerSupportService(SupportDAO supportDAO, OpenRouterClient openRouterClient) {
        this.supportDAO = supportDAO;
        this.openRouterClient = openRouterClient;
    }

    public ChatResult startConversation(int customerId, String name, String email, String phone,
                                        String subject, String content) throws SQLException {
        validateMessage(content);
        int requestId = supportDAO.create(customerId, name, email, phone, subject, content);
        return answerOrTransfer(requestId, customerId, subject, name, content);
    }

    public ChatResult sendMessage(int requestId, int customerId, String content) throws SQLException {
        validateMessage(content);
        Map<String, Object> request = supportDAO.customerRequest(requestId, customerId);
        if (request == null) throw new SQLException("Cuộc trò chuyện không thuộc khách hàng.");
        if ("DA_DONG".equals(String.valueOf(request.get("trangThai")))) {
            throw new SQLException("Cuộc trò chuyện đã đóng.");
        }

        supportDAO.customerMessage(requestId, customerId, content);
        request = supportDAO.customerRequest(requestId, customerId);
        if (request != null && request.get("maNhanVien") != null) {
            String staffName = supportDAO.assignedStaffName(requestId);
            supportDAO.touchRequest(requestId, "DANG_XU_LY");
            return new ChatResult(requestId, "", true, staffName, false, "WAITING_STAFF", List.of());
        }
        return answerOrTransfer(requestId, customerId,
                String.valueOf(request.getOrDefault("chuDe", "")),
                String.valueOf(request.getOrDefault("hoTen", "")), content);
    }

    private ChatResult answerOrTransfer(int requestId, int customerId, String subject,
                                        String customerName, String latestMessage) throws SQLException {
        if (asksForHuman(latestMessage)) {
            return transferToHuman(requestId,
                    "Mình đã chuyển yêu cầu của bạn đến nhân viên hỗ trợ để kiểm tra trực tiếp.");
        }

        List<Map<String, Object>> conversation = supportDAO.messages(requestId);
        OpenRouterClient.Result ai = openRouterClient.answer(conversation, subject, customerName);
        if (!ai.success()) {
            return transferToHuman(requestId,
                    "Hiện trợ lý AI tạm thời không phản hồi được. Yêu cầu của bạn đã được chuyển đến nhân viên hỗ trợ.");
        }

        boolean needsHuman = ai.message().contains(HUMAN_MARKER);
        String cleanReply = ai.message().replace(HUMAN_MARKER, "").trim();
        if (cleanReply.isBlank()) {
            cleanReply = needsHuman
                    ? "Vấn đề này cần nhân viên kiểm tra trực tiếp cho bạn."
                    : "C&C đã nhận được nội dung của bạn.";
        }
        supportDAO.insertBotMessage(requestId, cleanReply);

        if (needsHuman) {
            Map<String, Object> staff = supportDAO.assignAvailableStaff(requestId);
            if (staff == null) {
                String queueMessage = "Hiện các nhân viên đang bận. Yêu cầu của bạn đã được đưa vào hàng chờ ưu tiên.";
                supportDAO.insertBotMessage(requestId, queueMessage);
                supportDAO.touchRequest(requestId, "MOI");
                return new ChatResult(requestId, cleanReply + " " + queueMessage,
                        false, "", true, "QUEUED", List.of());
            }
            String staffName = String.valueOf(staff.getOrDefault("hoTen", "nhân viên hỗ trợ"));
            String assignedMessage = "Nhân viên " + staffName + " sẽ tiếp tục hỗ trợ bạn trong cuộc trò chuyện này.";
            supportDAO.insertBotMessage(requestId, assignedMessage);
            return new ChatResult(requestId, cleanReply + " " + assignedMessage,
                    true, staffName, true, "WAITING_STAFF", List.of());
        }

        supportDAO.touchRequest(requestId, "DA_PHAN_HOI");
        List<Map<String, Object>> products = productDAO.chatRecommendations(
                subject + " " + latestMessage + " " + cleanReply, 3);
        return new ChatResult(requestId, cleanReply, false, "", true, "AI_REPLIED", products);
    }

    private ChatResult transferToHuman(int requestId, String introduction) throws SQLException {
        Map<String, Object> staff = supportDAO.assignAvailableStaff(requestId);
        String reply;
        String staffName = "";
        boolean assigned = staff != null;
        if (assigned) {
            staffName = String.valueOf(staff.getOrDefault("hoTen", "nhân viên hỗ trợ"));
            reply = introduction + " Nhân viên " + staffName + " sẽ tiếp nhận cuộc trò chuyện này.";
        } else {
            supportDAO.touchRequest(requestId, "MOI");
            reply = introduction + " Hiện các nhân viên đang bận nên yêu cầu đã được đưa vào hàng chờ.";
        }
        supportDAO.insertBotMessage(requestId, reply);
        return new ChatResult(requestId, reply, assigned, staffName, false,
                assigned ? "WAITING_STAFF" : "QUEUED", List.of());
    }

    private boolean asksForHuman(String text) {
        String normalized = normalize(text);
        return containsAny(normalized,
                "gap nhan vien", "gap nguoi that", "nhan vien ho tro", "chuyen nhan vien",
                "noi chuyen voi nhan vien", "tu van vien", "goi nhan vien", "khieu nai",
                "doi nhan vien", "khong muon noi voi bot");
    }

    private void validateMessage(String content) {
        String value = content == null ? "" : content.trim();
        if (value.isBlank()) throw new IllegalArgumentException("Vui lòng nhập nội dung tin nhắn.");
        if (value.length() > 1800) throw new IllegalArgumentException("Tin nhắn tối đa 1.800 ký tự.");
    }

    private String normalize(String value) {
        String raw = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd');
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) if (value.contains(keyword)) return true;
        return false;
    }
}
