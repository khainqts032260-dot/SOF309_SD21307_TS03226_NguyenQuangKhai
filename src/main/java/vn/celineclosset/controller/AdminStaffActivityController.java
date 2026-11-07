package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.StaffActivityDAO;

import java.io.IOException;

@WebServlet("/admin/staff-activity")
public class AdminStaffActivityController extends BaseController {
    private final StaffActivityDAO activityDAO = new StaffActivityDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireOwnerRole(req, resp)) return;
        try {
            Integer staffId = positiveInt(req.getParameter("staffId"));
            Integer orderId = orderId(req.getParameter("orderId"));
            boolean invalidOrder = hasText(req.getParameter("orderId")) && orderId == null;
            boolean invalidStaff = hasText(req.getParameter("staffId")) && staffId == null;

            if (invalidOrder || invalidStaff) {
                req.setAttribute("filterError", "Dữ liệu lọc không hợp lệ. Mã đơn có thể nhập dạng 45, #45 hoặc DH00045.");
            }
            req.setAttribute("staffList", accountDAO.staff(false));
            req.setAttribute("activities", activityDAO.activities(staffId, orderId));
            view(req, resp, "admin/staff-activity.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private Integer orderId(String value) {
        if (!hasText(value)) return null;
        String normalized = value.trim().toUpperCase().replace("#", "").replace("DH", "").trim();
        return positiveInt(normalized);
    }

    private Integer positiveInt(String value) {
        if (!hasText(value)) return null;
        try {
            int number = Integer.parseInt(value.trim());
            return number > 0 ? number : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
