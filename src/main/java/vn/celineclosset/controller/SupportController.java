package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.SupportDAO;
import vn.celineclosset.dao.ProductDAO;
import vn.celineclosset.service.CustomerSupportService;

import java.io.IOException;
import java.util.Map;

@WebServlet("/support")
public class SupportController extends BaseController {
    private final SupportDAO supportDAO = new SupportDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final CustomerSupportService customerSupportService = new CustomerSupportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> user = auth(req);
            if (user != null && "CUSTOMER".equals(String.valueOf(user.get("vaiTro")))) {
                var requests = supportDAO.customerRequests(authId(req));
                req.setAttribute("requests", requests);
                String id = req.getParameter("id");
                if ((id == null || id.isBlank()) && !requests.isEmpty()) {
                    id = String.valueOf(requests.get(0).get("maYC"));
                }
                if (id != null && !id.isBlank()) {
                    int requestId = Integer.parseInt(id);
                    var selected = supportDAO.customerRequest(requestId, authId(req));
                    if (selected != null) {
                        req.setAttribute("selectedRequest", selected);
                        var messages = supportDAO.messages(requestId);
                        req.setAttribute("messages", messages);

                        StringBuilder recommendationContext = new StringBuilder(
                                String.valueOf(selected.getOrDefault("chuDe", "")));
                        int from = Math.max(0, messages.size() - 6);
                        for (int i = from; i < messages.size(); i++) {
                            recommendationContext.append(' ').append(
                                    String.valueOf(messages.get(i).getOrDefault("noiDung", "")));
                        }
                        req.setAttribute("chatProducts",
                                productDAO.chatRecommendations(recommendationContext.toString(), 3));
                    }
                }
            }
            view(req, resp, "support.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> user = auth(req);
            Integer customerId = user != null && "CUSTOMER".equals(String.valueOf(user.get("vaiTro")))
                    ? ((Number) user.get("maTK")).intValue() : null;
            if ("message".equals(req.getParameter("action"))) {
                if (customerId == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }
                int requestId = Integer.parseInt(req.getParameter("maYC"));
                customerSupportService.sendMessage(requestId, customerId, req.getParameter("noiDung"));
                resp.sendRedirect(req.getContextPath() + "/support?" + ("1".equals(req.getParameter("widget")) ? "widget=1&" : "") + "id=" + requestId);
            } else {
                int requestId;
                if (customerId != null) {
                    var result = customerSupportService.startConversation(customerId,
                            req.getParameter("hoTen"), req.getParameter("email"), req.getParameter("soDienThoai"),
                            req.getParameter("chuDe"), req.getParameter("noiDung"));
                    requestId = result.requestId();
                    resp.sendRedirect(req.getContextPath() + "/support?" + ("1".equals(req.getParameter("widget")) ? "widget=1&" : "") + "id=" + requestId + "&sent=1");
                } else {
                    requestId = supportDAO.create(null, req.getParameter("hoTen"), req.getParameter("email"),
                            req.getParameter("soDienThoai"), req.getParameter("chuDe"), req.getParameter("noiDung"));
                    resp.sendRedirect(req.getContextPath() + "/support?sent=1");
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
