package vn.celineclosset.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** DAO voucher: chỉ chứa các câu SQL đơn giản để dễ trình bày. */
public class VoucherDAO extends CrudDAO {

    public List<Map<String, Object>> all(String q) throws SQLException {
        String base = """
                SELECT v.*,
                       CONVERT(VARCHAR(16),v.ngayBatDau,120) AS ngayBatDauText,
                       CONVERT(VARCHAR(16),v.ngayKetThuc,120) AS ngayKetThucText,
                       CASE WHEN v.trangThai=0 THEN N'Tạm ngưng'
                            WHEN v.ngayBatDau>SYSDATETIME() THEN N'Sắp diễn ra'
                            WHEN v.ngayKetThuc IS NOT NULL AND v.ngayKetThuc<SYSDATETIME() THEN N'Hết hạn'
                            WHEN v.soLuot IS NOT NULL AND v.daDung>=v.soLuot THEN N'Hết lượt'
                            ELSE N'Đang hoạt động' END AS tinhTrang
                FROM VOUCHER v
                """;
        if (q == null || q.isBlank()) {
            return query(base + " ORDER BY v.maVoucher DESC");
        }
        String like = "%" + q.trim() + "%";
        return query(base + " WHERE v.maCode LIKE ? OR v.tenVoucher LIKE ? ORDER BY v.maVoucher DESC", like, like);
    }

    public Map<String, Object> byId(int id) throws SQLException {
        return queryOne("""
                SELECT v.*, CONVERT(VARCHAR(16),v.ngayBatDau,126) AS ngayBatDauInput,
                       CONVERT(VARCHAR(16),v.ngayKetThuc,126) AS ngayKetThucInput
                FROM VOUCHER v WHERE maVoucher=?
                """, id);
    }

    public void save(String idValue, String code, String name, String type, String value,
                     String maxDiscount, String minimumOrder, String startAt, String endAt,
                     String quantity, int status) throws SQLException {
        int id = parseInt(idValue, 0);
        String cleanCode = text(code).toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_-]", "");
        String cleanType = "FIXED".equals(type) ? "FIXED" : "PERCENT";
        BigDecimal discountValue = decimal(value, BigDecimal.ZERO);
        BigDecimal max = cleanType.equals("PERCENT") ? nullableDecimal(maxDiscount) : null;
        BigDecimal minimum = decimal(minimumOrder, BigDecimal.ZERO);
        Timestamp start = Timestamp.valueOf(parseDate(startAt, LocalDateTime.now()));
        Timestamp end = text(endAt).isBlank() ? null : Timestamp.valueOf(parseDate(endAt, null));
        Integer totalUses = parseInt(quantity, 0) <= 0 ? null : parseInt(quantity, 0);

        if (id <= 0) {
            executeUpdate("""
                    INSERT INTO VOUCHER(maCode,tenVoucher,loaiGiam,giaTri,giamToiDa,donToiThieu,diemDoi,
                                        ngayBatDau,ngayKetThuc,soLuot,daDung,trangThai)
                    VALUES(?,?,?,?,?,?,0,?,?,?,0,?)
                    """, cleanCode, text(name), cleanType, discountValue, max, minimum,
                    start, end, totalUses, status);
        } else {
            executeUpdate("""
                    UPDATE VOUCHER SET maCode=?,tenVoucher=?,loaiGiam=?,giaTri=?,giamToiDa=?,
                        donToiThieu=?,ngayBatDau=?,ngayKetThuc=?,soLuot=?,trangThai=?
                    WHERE maVoucher=?
                    """, cleanCode, text(name), cleanType, discountValue, max, minimum,
                    start, end, totalUses, status, id);
        }
    }

    public boolean codeExists(String code, int exceptId) throws SQLException {
        Map<String, Object> row = queryOne("SELECT COUNT(*) AS total FROM VOUCHER WHERE UPPER(maCode)=UPPER(?) AND maVoucher<>?",
                text(code), exceptId);
        return row != null && ((Number) row.get("total")).intValue() > 0;
    }

    public void toggle(int id, int status) throws SQLException {
        executeUpdate("UPDATE VOUCHER SET trangThai=? WHERE maVoucher=?", status, id);
    }

    private LocalDateTime parseDate(String value, LocalDateTime fallback) {
        if (value == null || value.isBlank()) return fallback;
        return LocalDateTime.parse(value.trim());
    }

    private BigDecimal decimal(String value, BigDecimal fallback) {
        BigDecimal result = nullableDecimal(value);
        return result == null ? fallback : result;
    }

    private BigDecimal nullableDecimal(String value) {
        try {
            String clean = text(value).replace(",", "");
            return clean.isBlank() ? null : new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return text(value).isBlank() ? fallback : Integer.parseInt(text(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }
}
