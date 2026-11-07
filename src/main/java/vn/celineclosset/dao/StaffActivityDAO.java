package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Đọc nhật ký công việc và lọc bằng tham số số đã được kiểm tra ở controller. */
public class StaffActivityDAO extends CrudDAO {
    public List<Map<String, Object>> activities(Integer staffId, Integer orderId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT nk.*, nv.hoTen AS tenNhanVien, nv.email AS emailNhanVien
                FROM NHAT_KY_NHAN_VIEN nk
                JOIN TAI_KHOAN nv ON nv.maTK=nk.maNhanVien
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (staffId != null) {
            sql.append(" AND nk.maNhanVien=?");
            params.add(staffId);
        }
        if (orderId != null) {
            sql.append(" AND nk.maDH=?");
            params.add(orderId);
        }
        sql.append(" ORDER BY nk.ngayTao DESC, nk.maNK DESC");
        return query(sql.toString(), params.toArray());
    }
}
