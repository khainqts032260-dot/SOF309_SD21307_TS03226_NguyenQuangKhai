package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.OrderDAO;
import vn.celineclosset.dao.LoyaltyDAO;
import vn.celineclosset.service.MapApiClient;
import vn.celineclosset.util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/checkout")
public class CheckoutController extends BaseController {
    private final OrderDAO orderDAO = new OrderDAO();
    private final LoyaltyDAO loyaltyDAO = new LoyaltyDAO();
    private final MapApiClient mapApiClient = new MapApiClient();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        try {
            loadCheckout(req);
            view(req, resp, "checkout.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        try {
            String phone = req.getParameter("phone");
            String addressArea = clean(req.getParameter("addressArea"));
            String addressDetail = clean(req.getParameter("addressDetail"));
            String address = joinAddress(addressDetail, addressArea);
            Double deliveryLat = optionalCoordinate(req.getParameter("deliveryLat"), -90, 90);
            Double deliveryLng = optionalCoordinate(req.getParameter("deliveryLng"), -180, 180);

            if (!ValidationUtil.isValidVietnamPhone(phone)) {
                req.setAttribute("error", ValidationUtil.phoneRuleMessage());
                loadCheckout(req);
                view(req, resp, "checkout.jsp");
                return;
            }
            if (addressArea.length() < 5) {
                req.setAttribute("error", "Vui lòng chọn khu vực giao hàng từ danh sách gợi ý hoặc đánh dấu trên bản đồ.");
                loadCheckout(req);
                view(req, resp, "checkout.jsp");
                return;
            }
            if (!ValidationUtil.isValidAddress(addressDetail) || !ValidationUtil.isValidAddress(address)) {
                req.setAttribute("error", "Địa chỉ chi tiết phải có số nhà và tên đường/hẻm/khu vực rõ ràng.");
                loadCheckout(req);
                view(req, resp, "checkout.jsp");
                return;
            }

            // Nếu khách chưa mở bản đồ, backend thử xác định tọa độ từ địa chỉ đã nhập.
            if (deliveryLat == null || deliveryLng == null) {
                var location = mapApiClient.searchFirst(address);
                if (location != null) {
                    deliveryLat = location.latitude();
                    deliveryLng = location.longitude();
                }
            }
            if (!orderDAO.validVoucher(req.getParameter("voucherCode"), cartDAO.cartTotal(authId(req), req.getParameterValues("selectedItemId")), authId(req))) {
                req.setAttribute("error", "Mã voucher không hợp lệ.");
                loadCheckout(req);
                req.setAttribute("voucherValid", false);
                req.setAttribute("voucherDiscount", BigDecimal.ZERO);
                req.setAttribute("payableTotal", req.getAttribute("total"));
                view(req, resp, "checkout.jsp");
                return;
            }

            String payment = "BANK".equalsIgnoreCase(req.getParameter("payment")) ? "BANK" : "COD";
            int orderId = orderDAO.createOrder(authId(req), req.getParameter("hoTenNhan"),
                    phone.trim(), address, addressArea, addressDetail, deliveryLat, deliveryLng,
                    req.getParameter("note"), payment,
                    req.getParameterValues("selectedItemId"), req.getParameter("voucherCode"));
            if ("BANK".equals(payment)) {
                resp.sendRedirect(req.getContextPath() + "/payment/bank?orderId=" + orderId);
            } else {
                resp.sendRedirect(req.getContextPath() + "/orders?success=" + orderId);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void loadCheckout(HttpServletRequest req) throws Exception {
        String[] selectedItemIds = req.getParameterValues("selectedItemId");
        String voucherCode = req.getParameter("voucherCode");
        BigDecimal total = cartDAO.cartTotal(authId(req), selectedItemIds);
        BigDecimal discount = orderDAO.voucherDiscount(voucherCode, total, authId(req));

        req.setAttribute("items", cartDAO.cartItems(authId(req), selectedItemIds));
        req.setAttribute("total", total);
        req.setAttribute("profile", accountDAO.accountById(authId(req)));
        req.setAttribute("voucherCode", voucherCode == null ? "" : voucherCode.trim().toUpperCase());
        req.setAttribute("voucherValid", orderDAO.validVoucher(voucherCode, total, authId(req)));
        req.setAttribute("myVouchers", loyaltyDAO.vouchers(authId(req)));
        req.setAttribute("publicVouchers", loyaltyDAO.publicVouchers());
        req.setAttribute("voucherDiscount", discount);
        req.setAttribute("payableTotal", total.subtract(discount).max(BigDecimal.ZERO));
        req.setAttribute("selectedItemIds", selectedItemIds);
    }
    private String joinAddress(String detail, String area) {
        if (detail.isBlank()) return area;
        if (area.isBlank()) return detail;
        return detail + ", " + area;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private Double optionalCoordinate(String value, double min, double max) {
        if (value == null || value.isBlank()) return null;
        try {
            double number = Double.parseDouble(value.trim());
            return number >= min && number <= max ? number : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}
