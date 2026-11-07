package vn.celineclosset.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Cấu hình ứng dụng; ưu tiên biến môi trường để không đưa mật khẩu thật vào source code. */
public final class AppConfig {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input != null) {
                // Properties.load(InputStream) mặc định dùng ISO-8859-1, dễ làm lỗi tiếng Việt.
                PROPERTIES.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        String environmentKey = key.toUpperCase().replace('.', '_');
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }
        return PROPERTIES.getProperty(key, "").trim();
    }

    public static String get(String key, String fallback) {
        String value = get(key);
        return value.isBlank() ? fallback : value;
    }

    public static boolean getBoolean(String key, boolean fallback) {
        String value = get(key);
        return value.isBlank() ? fallback : Boolean.parseBoolean(value);
    }

    public static int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
