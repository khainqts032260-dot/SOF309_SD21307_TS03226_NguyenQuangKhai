package vn.celineclosset.controller;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.NotificationDAO;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Trả thông báo mới cho ADMIN/STAFF/DELIVERY để hiện badge và toast ngay trên trang quản trị. */
@WebServlet("/api/notifications/unread")
public class BackOfficeNotificationApiController extends BaseController {
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        if (!requireBackOffice(req, resp)) return;
        try {
            int accountId = authId(req);
            List<Map<String, Object>> notifications = notificationDAO.unread(accountId, 8);
            int count = notificationDAO.unreadCount(accountId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("count", count);
            payload.put("items", notifications);
            String role = currentRole(req);
            payload.put("newOrderCount", "STAFF".equals(role)
                    ? commonOrderDAO.staffNewOrderCount(accountId) : 0);
            payload.put("activeDeliveryOrderCount", "DELIVERY".equals(role)
                    ? commonOrderDAO.deliveryActiveOrderCount(accountId) : 0);
            resp.getWriter().write(gson.toJson(payload));
            notificationDAO.markRead(accountId, notifications);
        } catch (Exception exception) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"count\":0,\"items\":[]}");
        }
    }
}
