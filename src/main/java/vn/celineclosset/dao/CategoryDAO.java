package vn.celineclosset.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CategoryDAO extends CrudDAO {

    public List<Map<String, Object>> categories(boolean activeOnly) throws SQLException {
        String sql = activeOnly
                ? "SELECT * FROM DANH_MUC WHERE trangThai=1 ORDER BY maDM DESC"
                : "SELECT * FROM DANH_MUC WHERE trangThai<>2 ORDER BY maDM DESC";
        return query(sql);
    }

    public Map<String, Object> category(int id) throws SQLException {
        return queryOne("SELECT * FROM DANH_MUC WHERE maDM=? AND trangThai<>2", id);
    }

    public void saveCategory(Map<String, String[]> params) throws SQLException {
        String id = val(params, "maDM");
        int status = Integer.parseInt(numberText(val(params, "trangThai"), "1"));
        if (id == null || id.isBlank()) {
            executeUpdate("INSERT INTO DANH_MUC(tenDM,moTa,trangThai) VALUES(?,?,?)",
                    text(val(params, "tenDM")), text(val(params, "moTa")), status);
        } else {
            executeUpdate("UPDATE DANH_MUC SET tenDM=?,moTa=?,trangThai=? WHERE maDM=?",
                    text(val(params, "tenDM")), text(val(params, "moTa")), status, Integer.parseInt(id));
        }
    }

    public void setCategoryStatus(int id, int status) throws SQLException {
        executeUpdate("UPDATE DANH_MUC SET trangThai=? WHERE maDM=?", status, id);
    }

    public void deleteCategory(int id) throws SQLException {
        // Xóa mềm để không làm lỗi các sản phẩm/đơn hàng đang tham chiếu danh mục này.
        setCategoryStatus(id, 2);
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String numberText(String value, String defaultValue) {
        String text = text(value).replace(",", "");
        return text.isBlank() ? defaultValue : text;
    }
}
