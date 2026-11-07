package vn.celineclosset.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.OrderDAO;
import vn.celineclosset.util.AppConfig;

import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/order-location")
public class OrderTrackingApiController extends BaseController {
    private final OrderDAO orderDAO = new OrderDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            if (!requireLoginJson(req, resp)) return;
            int orderId = Integer.parseInt(req.getParameter("id"));
            if (!canView(req, orderId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                write(resp, Map.of("success", false, "message", "Bạn không có quyền xem vị trí đơn hàng này."));
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("success", true);
            payload.put("order", orderDAO.trackingData(orderId));
            payload.put("history", orderDAO.trackingHistory(orderId));
            Map<String, Object> store = new LinkedHashMap<>();
            store.put("name", AppConfig.get("shop.name", "Celine Closet"));
            store.put("address", AppConfig.get("shop.address", "118/90/1 Liên khu 5-6, Khu phố 33, Phường Bình Tân, TP.HCM"));
            store.put("latitude", nullableDouble(AppConfig.get("shop.latitude")));
            store.put("longitude", nullableDouble(AppConfig.get("shop.longitude")));
            payload.put("store", store);
            write(resp, payload);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(resp, Map.of("success", false, "message", safeMessage(e)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            if (!requireLoginJson(req, resp)) return;
            String role = currentRole(req);
            int staffId = authId(req);
            if (!"DELIVERY".equals(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                write(resp, Map.of("success", false, "message", "Chỉ nhân viên giao đơn được cập nhật vị trí."));
                return;
            }
            double latitude = Double.parseDouble(req.getParameter("lat"));
            double longitude = Double.parseDouble(req.getParameter("lng"));
            boolean allOrders = "1".equals(req.getParameter("all"));
            if (allOrders) {
                int total = orderDAO.updateLocationForActiveOrders(staffId, latitude, longitude, req.getParameter("note"));
                if (total == 0) throw new IllegalStateException("Bạn chưa có đơn đang chuẩn bị hoặc đang giao.");
                write(resp, Map.of("success", true, "updatedOrders", total,
                        "message", "Đã gửi GPS cho " + total + " đơn đang giao."));
            } else {
                int orderId = Integer.parseInt(req.getParameter("id"));
                orderDAO.updateLocation(orderId, staffId, latitude, longitude, req.getParameter("note"));
                write(resp, Map.of("success", true, "updatedOrders", 1,
                        "message", "Đã cập nhật vị trí giao hàng."));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(resp, Map.of("success", false, "message", safeMessage(e)));
        }
    }

    private boolean requireLoginJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (auth(req) != null) return true;
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        write(resp, Map.of("success", false, "message", "Vui lòng đăng nhập."));
        return false;
    }

    private boolean canView(HttpServletRequest req, int orderId) throws Exception {
        String role = currentRole(req);

        // ADMIN được theo dõi toàn bộ. STAFF chỉ xem đơn do chính mình phụ trách.
        if ("ADMIN".equals(role)) {
            return orderDAO.orderById(orderId) != null;
        }
        if ("STAFF".equals(role)) {
            return orderDAO.processingStaffOwnsOrder(orderId, authId(req));
        }

        // DELIVERY chỉ được xem vị trí các đơn đã được phân công cho chính mình.
        if ("DELIVERY".equals(role)) {
            return orderDAO.staffOwnsOrder(orderId, authId(req));
        }

        // CUSTOMER chỉ được xem đơn thuộc tài khoản của mình.
        return orderDAO.myOrderById(authId(req), orderId) != null;
    }

    private Double nullableDouble(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            double parsed = Double.parseDouble(value.trim());
            return Double.isFinite(parsed) ? parsed : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String safeMessage(Exception exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Không thể xử lý yêu cầu vị trí." : exception.getMessage();
    }

    private void write(HttpServletResponse resp, Object value) throws IOException {
        resp.getWriter().write(gson.toJson(jsonSafe(value)));
    }

    private Object jsonSafe(Object value) {
        if (value == null || value instanceof Number || value instanceof Boolean || value instanceof String) return value;
        if (value instanceof TemporalAccessor || value instanceof Date) return value.toString();
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> clean = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                clean.put(String.valueOf(entry.getKey()), jsonSafe(entry.getValue()));
            }
            return clean;
        }
        if (value instanceof List<?> list) return list.stream().map(this::jsonSafe).toList();
        return String.valueOf(value);
    }
}
