package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeedbackDAO extends CrudDAO {
    private static final Object REVIEW_SCHEMA_LOCK = new Object();
    private static volatile boolean reviewSchemaReady;

    public void ensureReviewSchema() throws SQLException {
        if (reviewSchemaReady) return;
        synchronized (REVIEW_SCHEMA_LOCK) {
            if (reviewSchemaReady) return;
            executeUpdate("""
                    IF COL_LENGTH(N'dbo.PHAN_HOI',N'maDH') IS NULL
                        ALTER TABLE dbo.PHAN_HOI ADD maDH INT NULL;
                    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_PHAN_HOI_DON_HANG')
                        ALTER TABLE dbo.PHAN_HOI ADD CONSTRAINT FK_PHAN_HOI_DON_HANG
                            FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH);
                    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'UX_PHAN_HOI_DON_SAN_PHAM_KHACH' AND object_id=OBJECT_ID(N'dbo.PHAN_HOI'))
                        CREATE UNIQUE INDEX UX_PHAN_HOI_DON_SAN_PHAM_KHACH
                        ON dbo.PHAN_HOI(maDH,maSP,maTK)
                        WHERE maDH IS NOT NULL AND maSP IS NOT NULL AND maTK IS NOT NULL AND trangThai=1;
                    """);
            reviewSchemaReady = true;
        }
    }

    public List<Map<String, Object>> publicFeedbacks() throws SQLException {
        try {
            return query("""
                    SELECT TOP 8 ph.maPH,ph.maSP,ph.hoTen,ph.noiDung,ph.soSao,ph.hinhAnh,ph.daMuaHang,ph.ngayTao,
                           sp.tenSP
                    FROM PHAN_HOI ph LEFT JOIN SAN_PHAM sp ON sp.maSP=ph.maSP
                    WHERE ph.trangThai=1 ORDER BY ph.maPH DESC
                    """);
        } catch (SQLException e) {
            return defaultFeedbacks();
        }
    }

    public List<Map<String, Object>> productFeedbacks(int productId) throws SQLException {
        ensureDemoFeedbacks(productId);
        return query("""
                SELECT ph.maPH,ph.maTK,ph.maSP,ph.hoTen,ph.noiDung,ph.soSao,ph.hinhAnh,ph.daMuaHang,ph.ngayTao
                FROM PHAN_HOI ph WHERE ph.trangThai=1 AND ph.maSP=? ORDER BY ph.maPH DESC
                """, productId);
    }

    /** Bảo đảm mỗi sản phẩm luôn có đánh giá demo để giao diện không bị trống. */
    public void ensureDemoFeedbacks(int productId) throws SQLException {
        Map<String, Object> count = queryOne("SELECT COUNT(*) AS total FROM PHAN_HOI WHERE maSP=?", productId);
        int total = count == null || count.get("total") == null ? 0 : ((Number) count.get("total")).intValue();
        if (total > 0) return;

        String image = null;
        if (productId % 3 == 0) {
            Map<String, Object> product = queryOne("SELECT hinhAnh FROM SAN_PHAM WHERE maSP=?", productId);
            if (product != null && product.get("hinhAnh") != null) {
                image = product.get("hinhAnh").toString();
            }
        }
        executeUpdate("""
                INSERT INTO PHAN_HOI(maSP,hoTen,email,noiDung,soSao,hinhAnh,daMuaHang,trangThai)
                VALUES(?,?,?,?,?,?,1,1),(?,?,?,?,?,?,1,1)
                """,
                productId, "Ngọc Anh", "ngocanh.demo@celine.vn",
                "Sản phẩm đúng hình, phom gọn và chất liệu dễ chịu. Màu thực tế trang nhã, phù hợp mặc đi làm.", 5, image,
                productId, "Thảo My", "thaomy.demo@celine.vn",
                "Shop đóng gói cẩn thận, tư vấn size nhanh. Sản phẩm mặc lên cân đối và đường may khá đẹp.", 4, null);
    }

    public Map<String, Object> productSummary(int productId) throws SQLException {
        ensureDemoFeedbacks(productId);
        return queryOne("""
                SELECT COUNT(*) AS totalReviews,
                       CAST(COALESCE(AVG(CAST(soSao AS DECIMAL(10,2))),0) AS DECIMAL(10,1)) AS averageRating,
                       COALESCE(SUM(CASE WHEN soSao=5 THEN 1 ELSE 0 END),0) AS star5,
                       COALESCE(SUM(CASE WHEN soSao=4 THEN 1 ELSE 0 END),0) AS star4,
                       COALESCE(SUM(CASE WHEN soSao=3 THEN 1 ELSE 0 END),0) AS star3,
                       COALESCE(SUM(CASE WHEN soSao=2 THEN 1 ELSE 0 END),0) AS star2,
                       COALESCE(SUM(CASE WHEN soSao=1 THEN 1 ELSE 0 END),0) AS star1
                FROM PHAN_HOI WHERE trangThai=1 AND maSP=?
                """, productId);
    }

    public void saveFeedback(Integer maTK, Integer maSP, Integer maDH, String hoTen, String email,
                             String noiDung, String soSao, String imagePath) throws SQLException {
        ensureReviewSchema();
        if (maTK == null || maSP == null) {
            throw new SQLException("Bạn chỉ có thể đánh giá sau khi đơn hàng đã hoàn thành.");
        }
        Integer verifiedOrderId = completedOrderForReview(maTK, maSP, maDH);
        if (verifiedOrderId == null) {
            throw new SQLException("Không tìm thấy đơn hoàn thành có sản phẩm này hoặc đơn đã được đánh giá.");
        }
        if (alreadyReviewed(maTK, maSP, verifiedOrderId)) {
            throw new SQLException("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi.");
        }
        executeUpdate("""
                INSERT INTO PHAN_HOI(maTK,maSP,maDH,hoTen,email,noiDung,soSao,hinhAnh,daMuaHang,trangThai)
                VALUES(?,?,?,?,?,?,?,?,1,1)
                """, maTK, maSP, verifiedOrderId, text(hoTen), cleanEmail(email), text(noiDung),
                parseStar(soSao), emptyToNull(imagePath));
    }

    public void saveFeedback(Integer maTK, Integer maSP, String hoTen, String email, String noiDung,
                             String soSao, String imagePath) throws SQLException {
        saveFeedback(maTK, maSP, null, hoTen, email, noiDung, soSao, imagePath);
    }

    public void saveFeedback(Integer maTK, String hoTen, String email, String noiDung, String soSao) throws SQLException {
        saveFeedback(maTK, null, null, hoTen, email, noiDung, soSao, null);
    }

    public boolean alreadyReviewed(int accountId, int productId, int orderId) throws SQLException {
        ensureReviewSchema();
        return queryOne("""
                SELECT TOP 1 maPH FROM PHAN_HOI
                WHERE maTK=? AND maSP=? AND maDH=? AND trangThai=1
                """, accountId, productId, orderId) != null;
    }

    /**
     * Trả về đơn hàng hợp lệ để đánh giá sản phẩm. Chỉ chấp nhận đơn của chính khách hàng,
     * trạng thái Hoàn thành và sản phẩm trong đơn đó chưa được đánh giá.
     */
    public Integer reviewableOrderId(Integer accountId, Integer productId, Integer requestedOrderId) throws SQLException {
        if (accountId == null || productId == null) return null;
        ensureReviewSchema();
        Map<String, Object> row = requestedOrderId == null
                ? queryOne("""
                        SELECT TOP 1 dh.maDH FROM DON_HANG dh
                        JOIN CHI_TIET_DON_HANG ct ON ct.maDH=dh.maDH
                        WHERE dh.maTK=? AND ct.maSP=? AND dh.trangThai=N'Hoàn thành'
                          AND NOT EXISTS(SELECT 1 FROM PHAN_HOI ph WHERE ph.maTK=dh.maTK AND ph.maSP=ct.maSP
                                         AND ph.maDH=dh.maDH AND ph.trangThai=1)
                        ORDER BY COALESCE(dh.ngayHoanThanh,dh.ngayDat) DESC,dh.maDH DESC
                        """, accountId, productId)
                : queryOne("""
                        SELECT TOP 1 dh.maDH FROM DON_HANG dh
                        JOIN CHI_TIET_DON_HANG ct ON ct.maDH=dh.maDH
                        WHERE dh.maTK=? AND ct.maSP=? AND dh.trangThai=N'Hoàn thành' AND dh.maDH=?
                          AND NOT EXISTS(SELECT 1 FROM PHAN_HOI ph WHERE ph.maTK=dh.maTK AND ph.maSP=ct.maSP
                                         AND ph.maDH=dh.maDH AND ph.trangThai=1)
                        """, accountId, productId, requestedOrderId);
        return row == null ? null : ((Number) row.get("maDH")).intValue();
    }

    private Integer completedOrderForReview(int accountId, int productId, Integer requestedOrderId) throws SQLException {
        return reviewableOrderId(accountId, productId, requestedOrderId);
    }

    private List<Map<String, Object>> defaultFeedbacks() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(feedback("Minh Anh", "Vải đẹp, form thanh lịch, đóng gói cẩn thận."));
        rows.add(feedback("Hoài Thương", "Shop tư vấn nhiệt tình, màu ngoài đẹp hơn hình."));
        rows.add(feedback("Thùy Dương", "Mẫu công sở dễ mặc, giá hợp lý."));
        return rows;
    }

    private Map<String, Object> feedback(String name, String content) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("hoTen", name);
        row.put("noiDung", content);
        row.put("soSao", 5);
        row.put("daMuaHang", 0);
        return row;
    }

    private int parseStar(String value) {
        try {
            int star = Integer.parseInt(value == null ? "5" : value.trim());
            return Math.max(1, Math.min(5, star));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private String text(String value) { return value == null ? "" : value.trim(); }
    private String cleanEmail(String email) { return email == null ? "" : email.trim().toLowerCase(); }
    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
