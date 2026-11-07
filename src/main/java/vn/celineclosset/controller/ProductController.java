package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.CategoryDAO;
import vn.celineclosset.dao.FeedbackDAO;
import vn.celineclosset.dao.ProductDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@WebServlet({"/products", "/product-detail"})
public class ProductController extends BaseController {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/product-detail".equals(req.getServletPath())) {
                showProductDetail(req, resp);
                return;
            }
            showProductList(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void showProductDetail(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        int productId;
        try {
            productId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        req.setAttribute("product", productDAO.product(productId));
        List<Map<String, Object>> productImages;
        try {
            productImages = productDAO.productImages(productId);
        } catch (Exception ignored) {
            productImages = Collections.emptyList();
        }
        req.setAttribute("productImages", productImages);

        // Màu hiển thị được lấy trực tiếp từ các ảnh thật, theo đúng thứ tự gallery.
        // Nhờ đó tên màu không thể lệch với ảnh hoặc bị thiếu do dữ liệu mô tả cũ.
        LinkedHashMap<String, String> imageByColor = new LinkedHashMap<>();
        for (Map<String, Object> image : productImages) {
            String color = Objects.toString(image.get("mauSac"), "").trim();
            String path = Objects.toString(image.get("duongDan"), "").trim();
            if (!color.isBlank() && !path.isBlank()) {
                imageByColor.putIfAbsent(color, path);
            }
        }
        List<Map<String, String>> productColors = new ArrayList<>();
        imageByColor.forEach((name, image) -> productColors.add(Map.of("name", name, "image", image)));
        req.setAttribute("productColors", productColors);
        req.setAttribute("relatedProducts", productDAO.latestProducts());
        req.setAttribute("feedbacks", feedbackDAO.productFeedbacks(productId));
        req.setAttribute("feedbackSummary", feedbackDAO.productSummary(productId));

        // Chỉ mở form đánh giá khi khách đã mua sản phẩm trong một đơn Hoàn thành
        // và chính đơn đó chưa có đánh giá cho sản phẩm này.
        Map<String, Object> currentUser = auth(req);
        Integer accountId = currentUser == null || currentUser.get("maTK") == null
                ? null : ((Number) currentUser.get("maTK")).intValue();
        Integer requestedOrderId = parseNullableInt(req.getParameter("orderId"));
        Integer reviewOrderId = null;
        if (accountId != null) {
            try {
                reviewOrderId = feedbackDAO.reviewableOrderId(accountId, productId, requestedOrderId);
            } catch (Exception ignored) {
                // Không làm hỏng trang chi tiết nếu dữ liệu đánh giá cũ chưa được nâng cấp.
            }
        }
        req.setAttribute("canReview", reviewOrderId != null);
        req.setAttribute("reviewOrderId", reviewOrderId);

        req.setAttribute("soldCount", productDAO.soldQuantity(productId));
        view(req, resp, "product-detail.jsp");
    }

    private void showProductList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String categoryId = req.getParameter("cat");
        String[] price = resolvePriceRange(req.getParameter("price"));

        req.setAttribute("products", productDAO.products(
                req.getParameter("q"),
                categoryId,
                price[0],
                price[1],
                req.getParameter("size"),
                req.getParameter("sort"),
                false));
        req.setAttribute("categories", categoryDAO.categories(true));

        if (categoryId != null && !categoryId.isBlank()) {
            req.setAttribute("selectedCategory", categoryDAO.category(Integer.parseInt(categoryId)));
        }
        view(req, resp, "products.jsp");
    }


    private Integer parseNullableInt(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /** Đổi lựa chọn dễ hiểu trên giao diện thành giá tối thiểu và tối đa. */
    private String[] resolvePriceRange(String price) {
        if ("under300".equals(price)) return new String[]{null, "299999"};
        if ("300to500".equals(price)) return new String[]{"300000", "500000"};
        if ("500to800".equals(price)) return new String[]{"500000", "800000"};
        if ("over800".equals(price)) return new String[]{"800001", null};
        return new String[]{null, null};
    }
}
