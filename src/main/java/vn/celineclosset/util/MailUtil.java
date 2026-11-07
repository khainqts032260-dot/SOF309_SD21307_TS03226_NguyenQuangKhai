package vn.celineclosset.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

/** Gửi email SMTP. Trả về false nếu hệ thống chưa được cấu hình SMTP. */
public final class MailUtil {
    private MailUtil() {
    }

    public static boolean isConfigured() {
        return !AppConfig.get("smtp.host").isBlank()
                && !AppConfig.get("smtp.username").isBlank()
                && !AppConfig.get("smtp.password").isBlank();
    }

    public static boolean sendPasswordReset(String to, String name, String resetLink, int expiresMinutes) throws Exception {
        if (!isConfigured()) {
            return false;
        }
        String subject = "Đặt lại mật khẩu Celine Closet";
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:620px;margin:auto;color:#242424">
                  <h2 style="font-weight:500">Xin chào %s,</h2>
                  <p>Celine Closet nhận được yêu cầu đặt lại mật khẩu cho <b>tài khoản khách hàng</b> của bạn.</p>
                  <p><a href="%s" style="display:inline-block;background:#111;color:#fff;padding:13px 22px;text-decoration:none">ĐẶT LẠI MẬT KHẨU</a></p>
                  <p>Liên kết có hiệu lực trong <b>%d phút</b> và chỉ dùng được một lần.</p>
                  <p>Nếu bạn không gửi yêu cầu này, hãy bỏ qua email và không chia sẻ liên kết cho người khác.</p>
                  <hr style="border:0;border-top:1px solid #ddd"><small>Celine Closet · Hỗ trợ khách hàng</small>
                </div>
                """.formatted(escapeHtml(name), escapeHtml(resetLink), expiresMinutes);
        sendHtml(to, subject, html);
        return true;
    }

    public static void sendHtml(String to, String subject, String html) throws Exception {
        String host = AppConfig.get("smtp.host");
        int port = AppConfig.getInt("smtp.port", 587);
        String username = AppConfig.get("smtp.username");
        String password = AppConfig.get("smtp.password");
        boolean startTls = AppConfig.getBoolean("smtp.startTls", true);

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", String.valueOf(port));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", String.valueOf(startTls));
        properties.put("mail.smtp.starttls.required", String.valueOf(startTls));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        String fromAddress = AppConfig.get("smtp.from");
        if (fromAddress.isBlank()) fromAddress = username;
        String fromName = AppConfig.get("smtp.fromName", "Celine Closet");
        message.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setSentDate(new Date());
        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "bạn";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
