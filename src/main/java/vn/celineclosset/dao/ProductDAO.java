package vn.celineclosset.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.text.Normalizer;

/**
 * DAO sản phẩm.
 * Mỗi điều kiện lọc được ghép từng bước để sinh viên dễ trình bày luồng xử lý.
 */
public class ProductDAO extends CrudDAO {

    /** Hàm cũ được giữ lại để các trang quản trị không phải sửa. */
    public List<Map<String, Object>> products(String q, String cat, boolean admin) throws SQLException {
        return products(q, cat, null, null, null, null, admin);
    }

    /**
     * Tìm sản phẩm theo từ khóa, danh mục, khoảng giá, size và cách sắp xếp.
     */
    public List<Map<String, Object>> products(String q, String cat, String minPrice, String maxPrice,
                                               String size, String sort, boolean admin) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT sp.*, dm.tenDM
                FROM SAN_PHAM sp
                LEFT JOIN DANH_MUC dm ON sp.maDM=dm.maDM
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (admin) {
            sql.append("AND sp.trangThai<>2 ");
        } else {
            sql.append("AND sp.trangThai=1 AND dm.trangThai=1 ");
        }

        if (hasText(q)) {
            String like = "%" + q.trim() + "%";
            sql.append("AND (sp.maSKU LIKE ? OR sp.tenSP LIKE ? OR sp.moTa LIKE ? OR sp.mauSac LIKE ? OR sp.chatLieu LIKE ? OR dm.tenDM LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (hasText(cat)) {
            sql.append("AND sp.maDM=? ");
            params.add(Integer.parseInt(cat.trim()));
        }

        BigDecimal min = decimalOrNull(minPrice);
        BigDecimal max = decimalOrNull(maxPrice);
        if (min != null) {
            sql.append("AND sp.donGia>=? ");
            params.add(min);
        }
        if (max != null) {
            sql.append("AND sp.donGia<=? ");
            params.add(max);
        }

        if (hasText(size)) {
            String normalizedSize = size.trim().toUpperCase(Locale.ROOT);
            sql.append("AND (',' + REPLACE(UPPER(ISNULL(sp.kichThuoc,'')),' ','') + ',') LIKE ? ");
            params.add("%," + normalizedSize + ",%");
        }

        sql.append(orderBy(sort));
        return query(sql.toString(), params.toArray());
    }

    /** Sản phẩm mới nhất dùng ở trang chi tiết và bài viết. */
    public List<Map<String, Object>> latestProducts() throws SQLException {
        return query("""
                SELECT TOP 8 sp.*, dm.tenDM
                FROM SAN_PHAM sp
                LEFT JOIN DANH_MUC dm ON sp.maDM=dm.maDM
                WHERE sp.trangThai=1 AND dm.trangThai=1
                ORDER BY sp.maSP DESC
                """);
    }

    /**
     * Mỗi lần tải trang chủ SQL Server dùng NEWID() để lấy một nhóm sản phẩm khác.
     */
    public List<Map<String, Object>> randomProducts(int limit) throws SQLException {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        String sql = "SELECT TOP " + safeLimit + " sp.*, dm.tenDM "
                + "FROM SAN_PHAM sp "
                + "LEFT JOIN DANH_MUC dm ON sp.maDM=dm.maDM "
                + "WHERE sp.trangThai=1 AND dm.trangThai=1 "
                + "ORDER BY NEWID()";
        return query(sql);
    }

    public Map<String, Object> product(int id) throws SQLException {
        return queryOne("SELECT sp.*, dm.tenDM FROM SAN_PHAM sp LEFT JOIN DANH_MUC dm ON sp.maDM=dm.maDM WHERE sp.maSP=?", id);
    }

    /** Toàn bộ ảnh thật của một sản phẩm, theo đúng thứ tự gallery. */
    public List<Map<String, Object>> productImages(int productId) throws SQLException {
        return query("""
                SELECT maAnh, maSP, duongDan, mauSac, gocAnh, thuTu
                FROM HINH_ANH_SAN_PHAM
                WHERE maSP=?
                ORDER BY thuTu, maAnh
                """, productId);
    }

    /** Tổng số lượng đã bán thành công của một sản phẩm. */
    public int soldQuantity(int productId) throws SQLException {
        Map<String, Object> row = queryOne("""
                SELECT COALESCE(SUM(ct.soLuong),0) AS sold
                FROM CHI_TIET_DON_HANG ct
                JOIN DON_HANG dh ON dh.maDH=ct.maDH
                WHERE ct.maSP=? AND dh.trangThai=N'Hoàn thành'
                """, productId);
        return row == null || row.get("sold") == null ? 0 : ((Number) row.get("sold")).intValue();
    }

    /**
     * Gợi ý tối đa vài sản phẩm cho chatbox. Không gọi AI thêm lần nữa: hệ thống
     * lấy chính sản phẩm đang bán trong database rồi chấm điểm theo nội dung khách hỏi.
     */
    public List<Map<String, Object>> chatRecommendations(String text, int limit) throws SQLException {
        String normalized = normalizeForSearch(text);
        if (!hasProductIntent(normalized)) return new ArrayList<>();

        List<Map<String, Object>> rows = query("""
                SELECT TOP 100 sp.maSP,sp.maSKU,sp.tenSP,sp.moTa,sp.donGia,sp.hinhAnh,sp.mauSac,sp.kichThuoc,sp.chatLieu,dm.tenDM
                FROM SAN_PHAM sp
                LEFT JOIN DANH_MUC dm ON dm.maDM=sp.maDM
                WHERE sp.trangThai=1 AND (dm.trangThai=1 OR sp.maDM IS NULL)
                ORDER BY sp.maSP DESC
                """);
        if (rows.isEmpty()) return rows;

        Set<String> tokens = new HashSet<>();
        for (String token : normalized.split("\\s+")) {
            if (token.length() >= 3) tokens.add(token);
        }

        for (Map<String, Object> row : rows) {
            String haystack = normalizeForSearch(String.valueOf(row.getOrDefault("tenSP", "")) + " "
                    + String.valueOf(row.getOrDefault("tenDM", "")) + " "
                    + String.valueOf(row.getOrDefault("moTa", "")) + " "
                    + String.valueOf(row.getOrDefault("mauSac", "")) + " "
                    + String.valueOf(row.getOrDefault("chatLieu", "")) + " "
                    + String.valueOf(row.getOrDefault("maSKU", "")));
            int score = 0;
            for (String token : tokens) {
                if (haystack.contains(token)) score += token.length() >= 6 ? 3 : 1;
            }
            if (containsAny(normalized, "cong so", "di lam", "van phong", "office")) {
                if (containsAny(haystack, "blazer", "so mi", "vest", "quan", "chan vay", "dam")) score += 5;
            }
            if (containsAny(normalized, "du tiec", "tiec", "event")) {
                if (containsAny(haystack, "dam", "vay", "blazer")) score += 5;
            }
            if (normalized.contains("tui") && haystack.contains("tui")) score += 8;
            if (normalized.contains("giay") && haystack.contains("giay")) score += 8;
            if (containsAny(normalized, "ao so mi", "so mi") && haystack.contains("so mi")) score += 8;
            if (normalized.contains("blazer") && haystack.contains("blazer")) score += 8;
            if (normalized.contains("quan") && haystack.contains("quan")) score += 6;
            if (containsAny(normalized, "chan vay", "vay") && containsAny(haystack, "chan vay", "vay")) score += 6;
            row.put("chatScore", score);
        }

        rows.sort(Comparator.comparingInt((Map<String, Object> row) -> ((Number) row.get("chatScore")).intValue()).reversed()
                .thenComparingInt(row -> -((Number) row.get("maSP")).intValue()));
        int safeLimit = Math.max(1, Math.min(limit, 4));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int score = ((Number) row.get("chatScore")).intValue();
            if (score <= 0 && !containsAny(normalized, "san pham", "tu van", "phoi do", "size")) continue;
            row.remove("chatScore");
            result.add(row);
            if (result.size() >= safeLimit) break;
        }
        return result;
    }

    private boolean hasProductIntent(String normalized) {
        return containsAny(normalized, "san pham", "tu van", "size", "mau", "mac", "phoi do", "cong so", "di lam",
                "ao", "dam", "vay", "blazer", "vest", "so mi", "quan", "tui", "giay", "that lung", "phu kien");
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private String normalizeForSearch(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
        return normalized.replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** Lấy sản phẩm theo danh sách ID, giữ đúng thứ tự lookbook truyền vào. */
    public List<Map<String, Object>> productsByIds(int... ids) throws SQLException {
        if (ids == null || ids.length == 0) return new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();
        Object[] params = new Object[ids.length];
        StringBuilder order = new StringBuilder("CASE sp.maSP ");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) placeholders.append(',');
            placeholders.append('?');
            params[i] = ids[i];
            order.append("WHEN ").append(ids[i]).append(" THEN ").append(i).append(' ');
        }
        order.append("ELSE ").append(ids.length).append(" END");
        return query("SELECT sp.*, dm.tenDM FROM SAN_PHAM sp "
                + "LEFT JOIN DANH_MUC dm ON sp.maDM=dm.maDM "
                + "WHERE sp.trangThai=1 AND sp.maSP IN (" + placeholders + ") "
                + "ORDER BY " + order, params);
    }

    public void saveProduct(String maSP, String maSKU, String tenSP, String moTa, String donGia, String soLuongTon,
                            String trangThai, String tenDM, String hinhAnh, String mauSac,
                            String kichThuoc, String chatLieu) throws SQLException {
        int maDM = findOrCreateCategory(tenDM);
        String imagePath = hinhAnh == null ? "" : hinhAnh.trim();
        Object[] data = new Object[]{
                text(maSKU),
                text(tenSP),
                text(moTa),
                new BigDecimal(numberText(donGia, "0")),
                Integer.parseInt(numberText(soLuongTon, "0")),
                Integer.parseInt(numberText(trangThai, "1")),
                maDM,
                imagePath,
                text(mauSac),
                text(kichThuoc),
                text(chatLieu)
        };
        if (maSP == null || maSP.isBlank()) {
            executeUpdate("""
                    INSERT INTO SAN_PHAM(maSKU,tenSP,moTa,donGia,soLuongTon,trangThai,maDM,hinhAnh,mauSac,kichThuoc,chatLieu)
                    VALUES(?,?,?,?,?,?,?,?,?,?,?)
                    """, data);
        } else {
            Object[] all = Arrays.copyOf(data, data.length + 1);
            all[all.length - 1] = Integer.parseInt(maSP);
            executeUpdate("""
                    UPDATE SAN_PHAM
                    SET maSKU=?,tenSP=?,moTa=?,donGia=?,soLuongTon=?,trangThai=?,maDM=?,hinhAnh=?,mauSac=?,kichThuoc=?,chatLieu=?
                    WHERE maSP=?
                    """, all);
        }
    }

    public void setProductStatus(int id, int status) throws SQLException {
        executeUpdate("UPDATE SAN_PHAM SET trangThai=? WHERE maSP=?", status, id);
    }

    public void deleteProduct(int id) throws SQLException {
        setProductStatus(id, 2);
    }

    private String orderBy(String sort) {
        if ("priceAsc".equals(sort)) return "ORDER BY sp.donGia ASC, sp.maSP DESC";
        if ("priceDesc".equals(sort)) return "ORDER BY sp.donGia DESC, sp.maSP DESC";
        if ("nameAsc".equals(sort)) return "ORDER BY sp.tenSP ASC";
        return "ORDER BY sp.maSP DESC";
    }

    private BigDecimal decimalOrNull(String value) {
        if (!hasText(value)) return null;
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int findOrCreateCategory(String tenDM) throws SQLException {
        String categoryName = text(tenDM);
        if (categoryName.isBlank()) categoryName = "Chưa phân loại";
        final String finalCategoryName = categoryName;

        return inTransaction(entityManager -> {
            Map<String, Object> row = queryOne(entityManager,
                    "SELECT TOP 1 maDM, trangThai FROM DANH_MUC WHERE tenDM=? ORDER BY maDM DESC",
                    finalCategoryName);
            if (row != null) {
                int maDM = ((Number) row.get("maDM")).intValue();
                executeUpdate(entityManager, "UPDATE DANH_MUC SET trangThai=1 WHERE maDM=?", maDM);
                return maDM;
            }
            Map<String, Object> created = queryOne(entityManager, """
                    INSERT INTO DANH_MUC(tenDM,moTa,trangThai)
                    OUTPUT INSERTED.maDM AS maDM
                    VALUES(?,?,1)
                    """, finalCategoryName, "Danh mục được tạo nhanh từ trang quản lý sản phẩm");
            if (created == null || created.get("maDM") == null) {
                throw new SQLException("Không tạo được danh mục sản phẩm");
            }
            return ((Number) created.get("maDM")).intValue();
        });
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String numberText(String value, String defaultValue) {
        String text = text(value).replace(",", "");
        return text.isBlank() ? defaultValue : text;
    }
}
