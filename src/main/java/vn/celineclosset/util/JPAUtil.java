package vn.celineclosset.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Tạo EntityManagerFactory dùng chung cho toàn bộ website.
 * Có thể đổi cấu hình bằng biến môi trường hoặc VM arguments:
 * DB_URL, DB_USER, DB_PASSWORD.
 */
public final class JPAUtil {
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = createFactory();

    private JPAUtil() {
    }

    private static EntityManagerFactory createFactory() {
        Map<String, Object> properties = new HashMap<>();
        putIfPresent(properties, "jakarta.persistence.jdbc.url", value("DB_URL"));
        putIfPresent(properties, "jakarta.persistence.jdbc.user", value("DB_USER"));
        putIfPresent(properties, "jakarta.persistence.jdbc.password", value("DB_PASSWORD"));
        return Persistence.createEntityManagerFactory("CelineClossetPU", properties);
    }

    public static EntityManager createEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    public static void close() {
        if (ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }

    private static String value(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        String environmentValue = System.getenv(key);
        return environmentValue == null || environmentValue.isBlank()
                ? null
                : environmentValue.trim();
    }

    private static void putIfPresent(Map<String, Object> properties, String key, String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }
}
