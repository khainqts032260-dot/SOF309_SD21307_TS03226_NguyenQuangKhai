package vn.celineclosset.dao;

import java.time.LocalDate;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Báo cáo doanh thu: toàn bộ câu thống kê được đặt trong stored procedure SQL Server. */
public class RevenueReportDAO extends CrudDAO {

    public Map<String, Object> summary(String fromDate, String toDate) throws SQLException {
        return callOne("{call dbo.sp_BaoCaoTongQuan(?, ?)}", dateParam(fromDate), dateParam(toDate));
    }

    public List<Map<String, Object>> revenueByDate(String fromDate, String toDate) throws SQLException {
        List<Map<String, Object>> rows = call("{call dbo.sp_BaoCaoDoanhThuTheoNgay(?, ?)}",
                dateParam(fromDate), dateParam(toDate));
        return withBarPercent(rows, "doanhThu");
    }

    public List<Map<String, Object>> revenueByCategory(String fromDate, String toDate) throws SQLException {
        return call("{call dbo.sp_BaoCaoDoanhThuTheoDanhMuc(?, ?)}",
                dateParam(fromDate), dateParam(toDate));
    }

    public List<Map<String, Object>> topProducts(String fromDate, String toDate) throws SQLException {
        return call("{call dbo.sp_ThongKeSanPhamBanChay(?, ?, ?)}",
                dateParam(fromDate), dateParam(toDate), 10);
    }

    public List<Map<String, Object>> paymentStatusStats(String fromDate, String toDate) throws SQLException {
        return call("{call dbo.sp_BaoCaoTrangThaiThanhToan(?, ?)}",
                dateParam(fromDate), dateParam(toDate));
    }

    public List<Map<String, Object>> orderStatusStats(String fromDate, String toDate) throws SQLException {
        return call("{call dbo.sp_BaoCaoTrangThaiDonHang(?, ?)}",
                dateParam(fromDate), dateParam(toDate));
    }

    public List<Map<String, Object>> recentPaidOrders(String fromDate, String toDate) throws SQLException {
        return call("{call dbo.sp_BaoCaoDonHangGanDay(?, ?)}",
                dateParam(fromDate), dateParam(toDate));
    }

    private LocalDate dateParam(String date) {
        return date == null || date.isBlank() ? null : LocalDate.parse(date);
    }

    private List<Map<String, Object>> withBarPercent(List<Map<String, Object>> rows, String valueKey) {
        double max = 0;
        for (Map<String, Object> row : rows) {
            Object value = row.get(valueKey);
            if (value instanceof Number) {
                max = Math.max(max, ((Number) value).doubleValue());
            }
        }
        for (Map<String, Object> row : rows) {
            Object value = row.get(valueKey);
            double amount = value instanceof Number ? ((Number) value).doubleValue() : 0;
            long percent = max <= 0 ? 0 : Math.round(amount * 100 / max);
            if (percent > 0 && percent < 6) {
                percent = 6;
            }
            row.put("barPercent", percent);
        }
        return rows;
    }
}
