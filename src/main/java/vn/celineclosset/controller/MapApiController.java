package vn.celineclosset.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.service.MapApiClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Proxy bản đồ/địa chỉ: giữ API key ở backend, không đưa sang JavaScript.
 */
@WebServlet("/api/map")
public class MapApiController extends BaseController {
    private final MapApiClient mapApiClient = new MapApiClient();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        if (auth(req) == null) {
            write(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Map.of("success", false, "message", "Vui lòng đăng nhập."));
            return;
        }

        try {
            String action = firstNonBlank(req.getParameter("action"), "search");
            switch (action) {
                case "search" -> search(req, resp);
                case "reverse" -> reverse(req, resp);
                case "route" -> route(req, resp);
                default -> write(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Map.of("success", false, "message", "Tác vụ bản đồ không hợp lệ."));
            }
        } catch (Exception exception) {
            write(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("success", false, "message", safeMessage(exception)));
        }
    }

    private void search(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = firstNonBlank(req.getParameter("q"), "");
        Double lat = optionalDouble(req.getParameter("lat"));
        Double lng = optionalDouble(req.getParameter("lng"));
        var results = mapApiClient.search(query, lat, lng);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.put("results", results);
        payload.put("message", results.isEmpty() ? "Chưa tìm thấy địa chỉ phù hợp." : "");
        write(resp, HttpServletResponse.SC_OK, payload);
    }

    private void reverse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        double lat = requiredDouble(req.getParameter("lat"));
        double lng = requiredDouble(req.getParameter("lng"));
        var result = mapApiClient.reverse(lat, lng);
        if (result == null) {
            write(resp, HttpServletResponse.SC_NOT_FOUND,
                    Map.of("success", false, "message", "Chưa xác định được địa chỉ tại điểm này."));
            return;
        }
        write(resp, HttpServletResponse.SC_OK, Map.of("success", true, "result", result));
    }

    private void route(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        double fromLat = requiredDouble(req.getParameter("fromLat"));
        double fromLng = requiredDouble(req.getParameter("fromLng"));
        double toLat = requiredDouble(req.getParameter("toLat"));
        double toLng = requiredDouble(req.getParameter("toLng"));
        JsonObject route = mapApiClient.route(fromLat, fromLng, toLat, toLng);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.put("route", route);
        write(resp, HttpServletResponse.SC_OK, payload);
    }

    private Double optionalDouble(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private double requiredDouble(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Thiếu tọa độ.");
        return Double.parseDouble(value.trim());
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? "Không thể xử lý dữ liệu bản đồ lúc này." : message;
    }

    private void write(HttpServletResponse resp, int status, Object value) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(value));
    }
}
