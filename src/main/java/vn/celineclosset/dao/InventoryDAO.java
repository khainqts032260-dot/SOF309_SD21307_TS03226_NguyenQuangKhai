package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Quản lý tồn kho và lịch sử nhập hàng trên cùng database Celine Closet. */
public class InventoryDAO extends CrudDAO {

    public List<Map<String, Object>> inventory(String q, String stock) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT sp.maSP, sp.tenSP, sp.hinhAnh, sp.mauSac, sp.kichThuoc,
                       sp.soLuongTon, sp.trangThai, dm.tenDM,
                       CASE WHEN sp.soLuongTon = 0 THEN N'Hết hàng'
                            WHEN sp.soLuongTon <= 5 THEN N'Sắp hết'
                            ELSE N'Còn hàng' END AS tinhTrangKho
                FROM SAN_PHAM sp
                LEFT JOIN DANH_MUC dm ON dm.maDM = sp.maDM
                WHERE sp.trangThai <> 2
                """);
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (sp.tenSP LIKE ? OR CAST(sp.maSP AS VARCHAR(20)) LIKE ? OR dm.tenDM LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if ("out".equals(stock)) sql.append(" AND sp.soLuongTon = 0");
        if ("low".equals(stock)) sql.append(" AND sp.soLuongTon BETWEEN 1 AND 5");
        if ("available".equals(stock)) sql.append(" AND sp.soLuongTon > 5");
        sql.append(" ORDER BY sp.soLuongTon ASC, sp.maSP DESC");
        return query(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> importHistory(int limit) throws SQLException {
        return importHistory(null, null, null, null, limit);
    }

    /**
     * Lịch sử nhập kho được tách thành trang riêng và có bộ lọc theo sản phẩm/biên lai,
     * nhân viên, khoảng ngày. TOP được giới hạn để tránh tải quá nhiều dữ liệu một lần.
     */
    public List<Map<String, Object>> importHistory(String q, String staffId,
                                                    String dateFrom, String dateTo,
                                                    int limit) throws SQLException {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        StringBuilder sql = new StringBuilder("""
                SELECT TOP %d nk.maNhapKho, nk.maSP, sp.tenSP, nk.soLuongNhap,
                       nk.tonTruoc, nk.tonSau, nk.ghiChu, nk.soBienLai, nk.nhaCungCap, nk.xuatXu, nk.ngayNhap,
                       tk.maTK AS maNhanVien, tk.hoTen AS tenNhanVien, tk.vaiTro
                FROM NHAP_KHO nk
                JOIN SAN_PHAM sp ON sp.maSP = nk.maSP
                JOIN TAI_KHOAN tk ON tk.maTK = nk.maNhanVien
                WHERE 1=1
                """.formatted(safeLimit));
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim() + "%";
            sql.append(" AND (sp.tenSP LIKE ? OR CAST(sp.maSP AS VARCHAR(20)) LIKE ? ")
               .append("OR nk.soBienLai LIKE ? OR nk.nhaCungCap LIKE ? OR nk.xuatXu LIKE ?)");
            params.add(like); params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (staffId != null && !staffId.isBlank()) {
            try {
                sql.append(" AND nk.maNhanVien=?");
                params.add(Integer.parseInt(staffId.trim()));
            } catch (NumberFormatException ignored) {
                // Giá trị lọc không hợp lệ được bỏ qua thay vì làm hỏng trang lịch sử.
            }
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            sql.append(" AND nk.ngayNhap>=CAST(? AS DATE)");
            params.add(dateFrom.trim());
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sql.append(" AND nk.ngayNhap<DATEADD(DAY,1,CAST(? AS DATE))");
            params.add(dateTo.trim());
        }
        sql.append(" ORDER BY nk.ngayNhap DESC, nk.maNhapKho DESC");
        return query(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> inventoryStaffAccounts() throws SQLException {
        return query("""
                SELECT DISTINCT tk.maTK,tk.hoTen,tk.vaiTro
                FROM TAI_KHOAN tk
                JOIN NHAP_KHO nk ON nk.maNhanVien=tk.maTK
                WHERE tk.vaiTro IN ('ADMIN','STAFF')
                ORDER BY tk.hoTen,tk.maTK
                """);
    }

    public void importStock(int productId, int quantity, int staffId, String note, String receiptNo, String supplier, String origin) throws SQLException {
        if (quantity <= 0 || quantity > 100000) {
            throw new SQLException("Số lượng nhập phải từ 1 đến 100.000.");
        }
        inTransaction(entityManager -> {
            Map<String, Object> row = queryOne(entityManager,
                    "SELECT maSP, soLuongTon FROM SAN_PHAM WITH (UPDLOCK, ROWLOCK) WHERE maSP=? AND trangThai<>2",
                    productId);
            if (row == null) throw new SQLException("Không tìm thấy sản phẩm cần nhập kho.");
            int before = ((Number) row.get("soLuongTon")).intValue();
            long afterLong = (long) before + quantity;
            if (afterLong > Integer.MAX_VALUE) throw new SQLException("Số lượng tồn vượt giới hạn cho phép.");
            int after = (int) afterLong;
            executeUpdate(entityManager, "UPDATE SAN_PHAM SET soLuongTon=? WHERE maSP=?", after, productId);
            executeUpdate(entityManager, """
                    INSERT INTO NHAP_KHO(maSP,soLuongNhap,tonTruoc,tonSau,maNhanVien,ghiChu,soBienLai,nhaCungCap,xuatXu)
                    VALUES(?,?,?,?,?,?,?,?,?)
                    """, productId, quantity, before, after, staffId,
                    note == null || note.isBlank() ? null : note.trim(),
                    receiptNo == null || receiptNo.isBlank() ? null : receiptNo.trim(),
                    supplier == null || supplier.isBlank() ? null : supplier.trim(),
                    origin == null || origin.isBlank() ? null : origin.trim());
            return null;
        });
    }
}
