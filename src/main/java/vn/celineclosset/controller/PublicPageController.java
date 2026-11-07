package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.dao.CategoryDAO;
import vn.celineclosset.dao.FeedbackDAO;
import vn.celineclosset.dao.NewsCategoryDAO;
import vn.celineclosset.dao.NewsDAO;
import vn.celineclosset.dao.ProductDAO;

import java.io.IOException;

@WebServlet({
        "/home", "/about", "/brand-values", "/contact",
        "/lookbook", "/lookbook-detail", "/news", "/news-detail"
})
public class PublicPageController extends BaseController {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final NewsDAO newsDAO = new NewsDAO();
    private final NewsCategoryDAO newsCategoryDAO = new NewsCategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String path = req.getServletPath();
            switch (path) {
                case "/home" -> showHome(req, resp);
                case "/about" -> view(req, resp, "about.jsp");
                case "/brand-values" -> view(req, resp, "brand-values.jsp");
                case "/contact" -> view(req, resp, "contact.jsp");
                case "/lookbook" -> view(req, resp, "lookbook.jsp");
                case "/lookbook-detail" -> showLookbookDetail(req, resp);
                case "/news" -> {
                    req.setAttribute("newsCategories", newsCategoryDAO.all(true));
                    req.setAttribute("newsList", newsDAO.published(req.getParameter("type")));
                    view(req, resp, "news.jsp");
                }
                case "/news-detail" -> showNewsDetail(req, resp);
                default -> showHome(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void showHome(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setAttribute("featuredProducts", productDAO.randomProducts(12));
        req.setAttribute("categories", categoryDAO.categories(true));
        req.setAttribute("feedbacks", feedbackDAO.publicFeedbacks());
        view(req, resp, "home.jsp");
    }

    private void showNewsDetail(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id;
        try {
            id = Integer.parseInt(firstNonBlank(req.getParameter("id"), "0"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/news");
            return;
        }
        var news = newsDAO.publishedById(id);
        if (news == null) {
            resp.sendRedirect(req.getContextPath() + "/news");
            return;
        }
        req.setAttribute("news", news);
        req.setAttribute("relatedNews", newsDAO.published(
                news.get("maLoaiTin") == null ? null : String.valueOf(news.get("maLoaiTin"))));
        view(req, resp, "news-detail.jsp");
    }

    private void showLookbookDetail(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String slug = firstNonBlank(req.getParameter("slug"), "month-01");
        int month;
        try {
            month = Integer.parseInt(slug.replace("month-", ""));
        } catch (NumberFormatException e) {
            month = 1;
        }
        month = Math.max(1, Math.min(month, 8));

        String[] titles = {
                "Khởi đầu thanh lịch", "Sắc xuân tinh tế", "Nhịp điệu hiện đại", "Nét đẹp tự tin",
                "Thanh lịch mùa hạ", "Gam màu trung tính", "Dáng vẻ đô thị", "Dấu ấn vượt thời gian"
        };
        String[] leads = {
                "Những đường nét mềm và gam beige mở đầu hành trình công sở mới.",
                "Sơ mi, gile và chân váy tạo nên vẻ nữ tính gọn gàng cho đầu năm.",
                "Những món cơ bản được làm mới bằng tỷ lệ hiện đại và cách phối linh hoạt.",
                "Phom dáng rõ nét giúp người mặc tự tin trong mọi lịch trình.",
                "Chất liệu nhẹ và màu sáng cho những ngày làm việc mùa hạ.",
                "Đen, trắng, xám và nâu được phối theo tinh thần tối giản có chiều sâu.",
                "Trang phục thanh lịch chuyển động cùng nhịp sống thành thị.",
                "Những thiết kế bền vững về phong cách, dễ mặc qua nhiều mùa."
        };
        int[][] productSets = {
                {10, 11, 12, 16}, {2, 3, 1, 6}, {14, 7, 6, 15}, {17, 16, 13, 19},
                {9, 20, 15, 3}, {18, 19, 11, 12}, {4, 8, 5, 14}, {10, 12, 16, 20}
        };

        req.setAttribute("collectionMonth", month);
        req.setAttribute("collectionTitle", titles[month - 1]);
        req.setAttribute("collectionLead", leads[month - 1]);
        req.setAttribute("heroImage", String.format("lookbook/lookbook-%02d.png", month));
        req.setAttribute("lookbookProducts", productDAO.productsByIds(productSets[month - 1]));
        view(req, resp, "lookbook-detail.jsp");
    }
}
