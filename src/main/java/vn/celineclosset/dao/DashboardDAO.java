package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Dashboard chỉ gọi stored procedure, không để câu SQL thống kê trong Java. */
public class DashboardDAO extends CrudDAO {

    public Map<String, Object> stats() throws SQLException {
        return callOne("{call dbo.sp_DashboardTongQuan}");
    }

    public List<Map<String, Object>> revenueLast7Days() throws SQLException {
        return withBarPercent(call("{call dbo.sp_DashboardDoanhThu7Ngay}"), "doanhThu");
    }

    public List<Map<String, Object>> orderStatusStats() throws SQLException {
        return call("{call dbo.sp_DashboardTrangThaiDonHang}");
    }

    public List<Map<String, Object>> paymentStatusStats() throws SQLException {
        return call("{call dbo.sp_DashboardTrangThaiThanhToan}");
    }

    private List<Map<String, Object>> withBarPercent(List<Map<String, Object>> rows, String valueKey) {
        double max = 0;
        for (Map<String, Object> row : rows) {
            max = Math.max(max, number(row.get(valueKey)));
        }
        for (Map<String, Object> row : rows) {
            double value = number(row.get(valueKey));
            long percent = max <= 0 ? 0 : Math.round(value * 100 / max);
            if (percent > 0 && percent < 8) {
                percent = 8;
            }
            row.put("barPercent", percent);
        }
        return rows;
    }

    private double number(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }
}
