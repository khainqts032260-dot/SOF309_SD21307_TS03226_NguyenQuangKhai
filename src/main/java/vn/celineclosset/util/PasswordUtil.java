package vn.celineclosset.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordUtil {
    private PasswordUtil() {}
    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] data = md.digest((raw == null ? "" : raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : data) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không mã hóa được mật khẩu", e);
        }
    }
}
