package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.LoyaltyDAO;

import java.io.IOException;

@WebServlet("/loyalty")
public class LoyaltyController extends BaseController {
    private final LoyaltyDAO loyaltyDAO = new LoyaltyDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireCustomer(req, resp)) return;
        try {
            load(req);
            view(req, resp, "loyalty.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireCustomer(req, resp)) return;
        try {
            int rewardId = Integer.parseInt(req.getParameter("rewardId"));
            String rewardName = loyaltyDAO.redeem(authId(req), rewardId);
            resp.sendRedirect(req.getContextPath() + "/loyalty?success=" + java.net.URLEncoder.encode(rewardName, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            try { load(req); } catch (Exception ignored) { }
            view(req, resp, "loyalty.jsp");
        }
    }


    private void load(HttpServletRequest req) throws Exception {
        int accountId = authId(req);
        req.setAttribute("loyalty", loyaltyDAO.summary(accountId));
        req.setAttribute("myVouchers", loyaltyDAO.vouchers(accountId));
        req.setAttribute("publicVouchers", loyaltyDAO.publicVouchers());
        req.setAttribute("rewards", loyaltyDAO.rewards());
        req.setAttribute("pointHistory", loyaltyDAO.pointHistory(accountId));
        req.setAttribute("redemptions", loyaltyDAO.redemptionHistory(accountId));
    }
}
