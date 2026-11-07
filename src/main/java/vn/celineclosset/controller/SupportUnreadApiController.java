package vn.celineclosset.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.SupportDAO;

import java.io.IOException;

/** Polling nhỏ để STAFF thấy ngay khi AI chuyển khách sang người thật. */
@WebServlet("/api/support/unread")
public class SupportUnreadApiController extends BaseController {
    private final SupportDAO supportDAO = new SupportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        if (!requireBackOffice(req, resp)) return;
        String role = currentRole(req);
        JsonObject json = new JsonObject();
        try {
            int count = ("ADMIN".equals(role) || "STAFF".equals(role))
                    ? supportDAO.unreadForBackOffice(role, authId(req)) : 0;
            json.addProperty("success", true);
            json.addProperty("count", count);
            resp.getWriter().write(json.toString());
        } catch (Exception exception) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.addProperty("success", false);
            json.addProperty("count", 0);
            resp.getWriter().write(json.toString());
        }
    }
}
