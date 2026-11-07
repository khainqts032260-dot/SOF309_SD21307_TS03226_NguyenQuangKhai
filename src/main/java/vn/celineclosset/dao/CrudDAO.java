package vn.celineclosset.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.hibernate.query.NativeQuery;
import vn.celineclosset.util.JPAUtil;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DAO nền dùng JPA EntityManager.
 * Các câu SQL native vẫn đi qua JPA để giữ code đơn giản và không làm thay đổi
 * cấu trúc bảng hoặc chức năng hiện có của website.
 */
public abstract class CrudDAO {

    @FunctionalInterface
    protected interface JpaWork<T> {
        T execute(EntityManager entityManager) throws Exception;
    }

    protected List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        EntityManager entityManager = JPAUtil.createEntityManager();
        try {
            return query(entityManager, sql, params);
        } catch (Exception e) {
            throw toSqlException(e);
        } finally {
            entityManager.close();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List<Map<String, Object>> query(EntityManager entityManager, String sql, Object... params) {
        NativeQuery nativeQuery = entityManager
                .createNativeQuery(numberedParameters(sql))
                .unwrap(NativeQuery.class);

        bind(nativeQuery, params);
        nativeQuery.setTupleTransformer((tuple, aliases) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < tuple.length; i++) {
                String alias = aliases != null && i < aliases.length && aliases[i] != null
                        ? aliases[i]
                        : "column" + (i + 1);
                Object value = tuple[i];
                // SQL Server BIT thường được Hibernate trả về dưới dạng Boolean.
                // Các JSP cũ của project đang xử lý trạng thái theo 0/1, nên chuẩn hóa
                // Boolean thành Integer để tránh lỗi EL: Boolean không thể so sánh với Long.
                if (value instanceof Boolean booleanValue) {
                    value = booleanValue ? 1 : 0;
                }
                row.put(alias, value);
            }
            replaceLegacySvgProductImage(row);
            return row;
        });
        return (List<Map<String, Object>>) nativeQuery.getResultList();
    }

    protected Map<String, Object> queryOne(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    protected Map<String, Object> queryOne(EntityManager entityManager, String sql, Object... params) {
        List<Map<String, Object>> rows = query(entityManager, sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    protected List<Map<String, Object>> call(String procedureCall, Object... params) throws SQLException {
        return query(toExecSql(procedureCall), params);
    }

    protected Map<String, Object> callOne(String procedureCall, Object... params) throws SQLException {
        List<Map<String, Object>> rows = call(procedureCall, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    protected int executeUpdate(String sql, Object... params) throws SQLException {
        return inTransaction(entityManager -> executeUpdate(entityManager, sql, params));
    }

    @SuppressWarnings("rawtypes")
    protected int executeUpdate(EntityManager entityManager, String sql, Object... params) {
        NativeQuery nativeQuery = entityManager
                .createNativeQuery(numberedParameters(sql))
                .unwrap(NativeQuery.class);
        bind(nativeQuery, params);
        return nativeQuery.executeUpdate();
    }

    protected <T> T inTransaction(JpaWork<T> work) throws SQLException {
        EntityManager entityManager = JPAUtil.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            T result = work.execute(entityManager);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw toSqlException(e);
        } finally {
            entityManager.close();
        }
    }

    @SuppressWarnings("rawtypes")
    private void bind(NativeQuery query, Object... params) {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
    }

    /** Đổi dấu ? kiểu JDBC thành ?1, ?2... để JPA hiểu tham số vị trí. */
    private String numberedParameters(String sql) {
        StringBuilder result = new StringBuilder();
        boolean insideText = false;
        int index = 1;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'') {
                result.append(ch);
                if (insideText && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    result.append(sql.charAt(++i));
                } else {
                    insideText = !insideText;
                }
            } else if (ch == '?' && !insideText) {
                result.append('?').append(index++);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /** Đổi {call dbo.proc(?,?)} thành EXEC dbo.proc ?,? cho SQL Server. */
    private String toExecSql(String procedureCall) {
        String call = procedureCall == null ? "" : procedureCall.trim();
        if (call.startsWith("{") && call.endsWith("}")) {
            call = call.substring(1, call.length() - 1).trim();
        }
        if (call.regionMatches(true, 0, "call", 0, 4)) {
            call = call.substring(4).trim();
        }
        int openParenthesis = call.indexOf('(');
        if (openParenthesis < 0) {
            return "EXEC " + call;
        }
        int closeParenthesis = call.lastIndexOf(')');
        String procedureName = call.substring(0, openParenthesis).trim();
        String arguments = closeParenthesis > openParenthesis
                ? call.substring(openParenthesis + 1, closeParenthesis).trim()
                : "";
        return arguments.isBlank()
                ? "EXEC " + procedureName
                : "EXEC " + procedureName + " " + arguments;
    }

    private SQLException toSqlException(Exception exception) {
        if (exception instanceof SQLException sqlException) {
            return sqlException;
        }
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof SQLException sqlException) {
                return sqlException;
            }
            cause = cause.getCause();
        }
        return new SQLException(exception.getMessage(), exception);
    }

    /** Chỉ thay ảnh SVG cũ; ảnh người dùng tải lên phải được giữ nguyên. */
    private void replaceLegacySvgProductImage(Map<String, Object> row) {
        Object imageValue = row.get("hinhAnh");
        if (!(imageValue instanceof String imagePath)
                || !imagePath.toLowerCase(Locale.ROOT).endsWith(".svg")) {
            return;
        }
        row.put("hinhAnh", "assets/images/fashion/card-01.jpg");
    }

    protected String val(Map<String, String[]> params, String key) {
        String[] values = params.get(key);
        return values == null || values.length == 0 ? "" : values[0];
    }
}
