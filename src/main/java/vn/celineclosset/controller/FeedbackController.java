package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.FeedbackDAO;
import vn.celineclosset.util.FileUploadUtil;

import java.io.IOException;
import java.util.Map;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 8 * 1024 * 1024)
@WebServlet("/feedback")
public class FeedbackController extends BaseController {
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> user = auth(req);
            Integer accountId = user == null ? null : ((Number) user.get("maTK")).intValue();
            Integer productId = parseNullableInt(req.getParameter("productId"));
            String name = firstNonBlank(req.getParameter("hoTen"), user == null ? "" : String.valueOf(user.get("hoTen")));
            String email = firstNonBlank(req.getParameter("email"), user == null ? "" : String.valueOf(user.get("email")));

            Integer orderIdValue = parseNullableInt(req.getParameter("orderId"));
            Integer verifiedOrderId = feedbackDAO.reviewableOrderId(accountId, productId, orderIdValue);
            if (verifiedOrderId == null) {
                throw new IllegalStateException("Bạn chỉ có thể đánh giá sản phẩm đã mua trong đơn hàng Hoàn thành và chưa được đánh giá.");
            }

            // Chỉ lưu ảnh sau khi đã xác minh quyền đánh giá, tránh upload rác bằng request thủ công.
            String imagePath = FileUploadUtil.uploadImage(req, "feedbackImage", "feedback", "/assets/uploads/feedbacks");
            feedbackDAO.saveFeedback(accountId, productId, verifiedOrderId, name, email,
                    req.getParameter("noiDung"), req.getParameter("soSao"), imagePath);
            String orderId = req.getParameter("orderId");
            if (orderId != null && !orderId.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId + "&reviewed=1");
            } else if (productId != null) {
                resp.sendRedirect(req.getContextPath() + "/product-detail?id=" + productId + "&feedback=success#product-reviews");
            } else {
                resp.sendRedirect(req.getContextPath() + "/home?feedback=success#feedback");
            }
        } catch (Exception e) {
            String productId = req.getParameter("productId");
            String orderId = req.getParameter("orderId");
            req.getSession().setAttribute("feedbackError", e.getMessage());
            if (orderId != null && !orderId.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/orders?id=" + orderId + "&reviewError=1");
            } else {
                resp.sendRedirect(req.getContextPath() + (productId == null ? "/home" : "/product-detail?id=" + productId + "#product-reviews"));
            }
        }
    }

    private Integer parseNullableInt(String value) {
        try { return value == null || value.isBlank() ? null : Integer.parseInt(value.trim()); }
        catch (NumberFormatException ignored) { return null; }
    }
}
