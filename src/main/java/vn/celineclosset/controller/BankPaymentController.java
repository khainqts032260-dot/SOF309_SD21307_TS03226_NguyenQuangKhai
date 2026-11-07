package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.PaymentDAO;
import vn.celineclosset.service.BankTransferService;
import vn.celineclosset.util.AppConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/payment/bank")
public class BankPaymentController extends BaseController {
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BankTransferService bankTransferService = new BankTransferService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            int expirationMinutes = AppConfig.getInt("payment.expirationMinutes", 10);
            paymentDAO.expirePendingBankPayments(expirationMinutes);
            if ("1".equals(req.getParameter("resume"))) {
                paymentDAO.resumeCancelledBankPayment(authId(req), orderId);
            }
            String expectedPaymentCode = bankTransferService.paymentCode(orderId);
            paymentDAO.ensurePaymentCodeForCustomer(authId(req), orderId, expectedPaymentCode);
            Map<String, Object> payment = paymentDAO.paymentForCustomer(authId(req), orderId);
            if (payment == null || !"BANK".equals(String.valueOf(payment.get("phuongThuc")))) {
                resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId);
                return;
            }
            BigDecimal amount = payment.get("soTien") instanceof BigDecimal value
                    ? value : new BigDecimal(String.valueOf(payment.get("soTien")));
            String paymentCode = expectedPaymentCode;
            List<Map<String, Object>> paymentItems = paymentDAO.paymentItemsForCustomer(authId(req), orderId);
            req.setAttribute("payment", payment);
            req.setAttribute("paymentItems", paymentItems);
            req.setAttribute("paymentItemCount", paymentItems.stream()
                    .mapToInt(item -> ((Number) item.get("soLuong")).intValue()).sum());
            req.setAttribute("bankConfigured", bankTransferService.isBankConfigured());
            req.setAttribute("paymentQrUrl", bankTransferService.qrImageUrl(amount, paymentCode));
            req.setAttribute("paymentCode", paymentCode);
            req.setAttribute("paymentExpiresMinutes", expirationMinutes);
            req.setAttribute("paymentSecondsRemaining",
                    paymentDAO.paymentSecondsRemaining(authId(req), orderId, expirationMinutes));
            view(req, resp, "bank-payment.jsp");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
