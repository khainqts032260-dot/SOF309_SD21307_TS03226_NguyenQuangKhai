package vn.celineclosset.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartDAO extends CrudDAO {

    public int getOrCreateCart(int maTK) throws SQLException {
        return inTransaction(entityManager -> {
            Map<String, Object> cart = queryOne(entityManager,
                    "SELECT TOP 1 maGH FROM GIO_HANG WHERE maTK=? AND trangThai=1 ORDER BY maGH DESC",
                    maTK);
            if (cart != null) {
                return ((Number) cart.get("maGH")).intValue();
            }

            Map<String, Object> created = queryOne(entityManager, """
                    INSERT INTO GIO_HANG(maTK,trangThai)
                    OUTPUT INSERTED.maGH AS maGH
                    VALUES(?,1)
                    """, maTK);
            if (created == null || created.get("maGH") == null) {
                throw new SQLException("Không tạo được giỏ hàng");
            }
            return ((Number) created.get("maGH")).intValue();
        });
    }

    public int addCart(int maTK, int maSP, int qty, String selectedColor, String selectedSize) throws SQLException {
        int quantity = Math.max(1, qty);
        int maGH = getOrCreateCart(maTK);
        Map<String, Object> product = queryOne(
                "SELECT maSP, donGia, soLuongTon FROM SAN_PHAM WHERE maSP=? AND trangThai=1",
                maSP);
        if (product == null) {
            throw new SQLException("Sản phẩm không tồn tại hoặc đang ngừng bán");
        }

        BigDecimal price = (BigDecimal) product.get("donGia");
        int stock = ((Number) product.get("soLuongTon")).intValue();
        if (quantity > stock) throw new SQLException("Số lượng đặt vượt quá tồn kho hiện tại.");
        String color = selectedColor == null || selectedColor.isBlank() ? null : selectedColor.trim();
        String size = selectedSize == null || selectedSize.isBlank() ? null : selectedSize.trim();
        Map<String, Object> exists = queryOne(
                "SELECT maCTGH, soLuong FROM CHI_TIET_GIO_HANG WHERE maGH=? AND maSP=? AND ISNULL(mauSac,'')=ISNULL(?,'') AND ISNULL(kichThuoc,'')=ISNULL(?,'')",
                maGH, maSP, color, size);

        if (exists == null) {
            executeUpdate("""
                    INSERT INTO CHI_TIET_GIO_HANG(maGH,maSP,soLuong,donGia,giamGia,thanhTien,mauSac,kichThuoc)
                    VALUES(?,?,?,?,0,?,?,?)
                    """, maGH, maSP, quantity, price, price.multiply(BigDecimal.valueOf(quantity)), color, size);
        } else {
            int newQty = ((Number) exists.get("soLuong")).intValue() + quantity;
            if (newQty > stock) throw new SQLException("Số lượng trong giỏ vượt quá tồn kho hiện tại.");
            executeUpdate("""
                    UPDATE CHI_TIET_GIO_HANG
                    SET soLuong=?, donGia=?, thanhTien=?
                    WHERE maCTGH=?
                    """, newQty, price, price.multiply(BigDecimal.valueOf(newQty)), exists.get("maCTGH"));
        }
        Map<String,Object> saved = queryOne("SELECT TOP 1 maCTGH FROM CHI_TIET_GIO_HANG WHERE maGH=? AND maSP=? AND ISNULL(mauSac,'')=ISNULL(?,'') AND ISNULL(kichThuoc,'')=ISNULL(?,'') ORDER BY maCTGH DESC", maGH, maSP, color, size);
        return ((Number)saved.get("maCTGH")).intValue();
    }

    public void addCart(int maTK, int maSP, int qty) throws SQLException {
        addCart(maTK, maSP, qty, null, null);
    }

    public List<Map<String, Object>> cartItems(int maTK) throws SQLException {
        int maGH = getOrCreateCart(maTK);
        return cartItemsByCartId(maGH);
    }

    public List<Map<String, Object>> cartItems(int maTK, String[] selectedItemIds) throws SQLException {
        int maGH = getOrCreateCart(maTK);
        List<Integer> ids = parseIds(selectedItemIds);
        if (ids.isEmpty()) {
            return cartItemsByCartId(maGH);
        }
        String placeholders = placeholders(ids.size());
        List<Object> params = new ArrayList<>();
        params.add(maGH);
        params.addAll(ids);
        return query("""
                SELECT ct.*, sp.tenSP, COALESCE(img.duongDan,sp.hinhAnh) AS hinhAnh, sp.soLuongTon, sp.maDM, dm.tenDM
                FROM CHI_TIET_GIO_HANG ct
                JOIN SAN_PHAM sp ON ct.maSP=sp.maSP
                LEFT JOIN DANH_MUC dm ON dm.maDM=sp.maDM
                OUTER APPLY (SELECT TOP 1 ha.duongDan FROM HINH_ANH_SAN_PHAM ha WHERE ha.maSP=ct.maSP AND (ct.mauSac IS NULL OR LTRIM(RTRIM(ha.mauSac))=LTRIM(RTRIM(ct.mauSac))) ORDER BY ha.thuTu,ha.maAnh) img
                WHERE ct.maGH=? AND ct.maCTGH IN (""" + placeholders + ") ORDER BY ct.maCTGH DESC",
                params.toArray());
    }

    public BigDecimal cartTotal(int maTK) throws SQLException {
        return sum(cartItems(maTK));
    }

    public BigDecimal cartTotal(int maTK, String[] selectedItemIds) throws SQLException {
        return sum(cartItems(maTK, selectedItemIds));
    }

    public int cartItemCount(int maTK) throws SQLException {
        Map<String, Object> row = queryOne("""
                SELECT COALESCE(SUM(ct.soLuong),0) AS cartCount
                FROM GIO_HANG gh
                LEFT JOIN CHI_TIET_GIO_HANG ct ON gh.maGH=ct.maGH
                WHERE gh.maTK=? AND gh.trangThai=1
                """, maTK);
        if (row == null || row.get("cartCount") == null) {
            return 0;
        }
        return ((Number) row.get("cartCount")).intValue();
    }

    private BigDecimal sum(List<Map<String, Object>> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            total = total.add((BigDecimal) item.get("thanhTien"));
        }
        return total;
    }

    public void updateCart(int maTK, int maCTGH, int qty) throws SQLException {
        int maGH = getOrCreateCart(maTK);
        if (qty <= 0) {
            executeUpdate("DELETE FROM CHI_TIET_GIO_HANG WHERE maCTGH=? AND maGH=?", maCTGH, maGH);
        } else {
            executeUpdate("""
                    UPDATE CHI_TIET_GIO_HANG
                    SET soLuong=?, thanhTien=donGia * ?
                    WHERE maCTGH=? AND maGH=?
                    """, qty, qty, maCTGH, maGH);
        }
    }

    public void changeQuantity(int maTK, int maCTGH, int delta) throws SQLException {
        int maGH = getOrCreateCart(maTK);
        Map<String, Object> item = queryOne(
                "SELECT maCTGH, soLuong FROM CHI_TIET_GIO_HANG WHERE maCTGH=? AND maGH=?",
                maCTGH, maGH);
        if (item == null) {
            return;
        }
        int newQty = ((Number) item.get("soLuong")).intValue() + delta;
        updateCart(maTK, maCTGH, newQty);
    }

    public void removeCartItem(int maTK, int maCTGH) throws SQLException {
        int maGH = getOrCreateCart(maTK);
        executeUpdate("DELETE FROM CHI_TIET_GIO_HANG WHERE maCTGH=? AND maGH=?", maCTGH, maGH);
    }

    public List<Map<String, Object>> allCarts(String q) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT gh.maGH, gh.maTK, gh.ngayTao, gh.trangThai,
                       tk.hoTen, tk.email, tk.soDienThoai,
                       COUNT(ct.maCTGH) AS soMatHang,
                       COALESCE(SUM(ct.thanhTien),0) AS tongTien
                FROM GIO_HANG gh
                JOIN TAI_KHOAN tk ON gh.maTK=tk.maTK
                LEFT JOIN CHI_TIET_GIO_HANG ct ON gh.maGH=ct.maGH
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            String like = "%" + q.trim() + "%";
            sql.append("AND (CAST(gh.maGH AS VARCHAR(20)) LIKE ? OR tk.hoTen LIKE ? OR tk.email LIKE ? OR tk.soDienThoai LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append("GROUP BY gh.maGH, gh.maTK, gh.ngayTao, gh.trangThai, tk.hoTen, tk.email, tk.soDienThoai ");
        sql.append("ORDER BY gh.ngayTao DESC");
        return query(sql.toString(), params.toArray());
    }

    public Map<String, Object> cartById(int maGH) throws SQLException {
        return queryOne("""
                SELECT gh.maGH, gh.maTK, gh.ngayTao, gh.trangThai,
                       tk.hoTen, tk.email, tk.soDienThoai
                FROM GIO_HANG gh
                JOIN TAI_KHOAN tk ON gh.maTK=tk.maTK
                WHERE gh.maGH=?
                """, maGH);
    }

    public List<Map<String, Object>> cartItemsByCartId(int maGH) throws SQLException {
        return query("""
                SELECT ct.*, sp.tenSP, COALESCE(img.duongDan,sp.hinhAnh) AS hinhAnh, sp.soLuongTon, sp.maDM, dm.tenDM
                FROM CHI_TIET_GIO_HANG ct
                JOIN SAN_PHAM sp ON ct.maSP=sp.maSP
                LEFT JOIN DANH_MUC dm ON dm.maDM=sp.maDM
                OUTER APPLY (SELECT TOP 1 ha.duongDan FROM HINH_ANH_SAN_PHAM ha WHERE ha.maSP=ct.maSP AND (ct.mauSac IS NULL OR LTRIM(RTRIM(ha.mauSac))=LTRIM(RTRIM(ct.mauSac))) ORDER BY ha.thuTu,ha.maAnh) img
                WHERE ct.maGH=?
                ORDER BY ct.maCTGH DESC
                """, maGH);
    }

    public void updateCartItemAdmin(int maCTGH, int qty) throws SQLException {
        if (qty <= 0) {
            removeCartItemAdmin(maCTGH);
        } else {
            executeUpdate("""
                    UPDATE CHI_TIET_GIO_HANG
                    SET soLuong=?, thanhTien=donGia * ?
                    WHERE maCTGH=?
                    """, qty, qty, maCTGH);
        }
    }

    public void removeCartItemAdmin(int maCTGH) throws SQLException {
        executeUpdate("DELETE FROM CHI_TIET_GIO_HANG WHERE maCTGH=?", maCTGH);
    }

    public void clearCartAdmin(int maGH) throws SQLException {
        executeUpdate("DELETE FROM CHI_TIET_GIO_HANG WHERE maGH=?", maGH);
    }

    public void toggleCartStatus(int maGH, int status) throws SQLException {
        executeUpdate("UPDATE GIO_HANG SET trangThai=? WHERE maGH=?", status, maGH);
    }

    private List<Integer> parseIds(String[] values) {
        List<Integer> ids = new ArrayList<>();
        if (values == null) {
            return ids;
        }
        for (String value : values) {
            try {
                if (value != null && !value.isBlank()) {
                    ids.add(Integer.parseInt(value.trim()));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private String placeholders(int size) {
        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }
}
