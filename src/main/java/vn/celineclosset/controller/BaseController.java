package vn.celineclosset.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.celineclosset.dao.AccountDAO;
import vn.celineclosset.dao.CartDAO;
import vn.celineclosset.dao.CategoryDAO;
import vn.celineclosset.dao.WishlistDAO;
import vn.celineclosset.dao.SupportDAO;
import vn.celineclosset.dao.OrderDAO;
import vn.celineclosset.dao.NotificationDAO;
import vn.celineclosset.util.AppConfig;

import java.io.IOException;
import java.util.Map;

/**
 * Controller nền: chỉ giữ các hàm dùng chung như mở JSP, lấy tài khoản đăng nhập
 * và kiểm tra quyền. Mỗi chức năng website nằm trong một controller riêng.
 */
public abstract class BaseController extends HttpServlet {
    protected final AccountDAO accountDAO = new AccountDAO();
    protected final CartDAO cartDAO = new CartDAO();
    protected final CategoryDAO categoryDAO = new CategoryDAO();
    protected final WishlistDAO wishlistDAO = new WishlistDAO();
    protected final SupportDAO supportDAO = new SupportDAO();
    protected final OrderDAO commonOrderDAO = new OrderDAO();
    protected final NotificationDAO commonNotificationDAO = new NotificationDAO();

    protected void view(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.getRequestDispatcher("/WEB-INF/views/" + jsp).forward(req, resp);
    }

    private void setCommonAttributes(HttpServletRequest req) throws ServletException {
        try {
            Map<String, Object> user = refreshAuthenticatedUser(req);
            int cartCount = 0;
            if (user != null && "CUSTOMER".equals(String.valueOf(user.get("vaiTro")))) {
                int accountId = accountId(user);
                cartCount = cartDAO.cartItemCount(accountId);
                try {
                    req.setAttribute("wishlistMap", wishlistDAO.productIdMap(accountId));
                    req.setAttribute("wishlistCount", wishlistDAO.count(accountId));
                } catch (Exception ignored) {
                    req.setAttribute("wishlistMap", java.util.Collections.emptyMap());
                    req.setAttribute("wishlistCount", 0);
                }
            } else {
                req.setAttribute("wishlistMap", java.util.Collections.emptyMap());
                req.setAttribute("wishlistCount", 0);
            }
            req.setAttribute("cartCount", cartCount);
            int supportUnreadCount = 0;
            if (user != null) {
                String role = String.valueOf(user.get("vaiTro"));
                if ("ADMIN".equals(role) || "STAFF".equals(role)) {
                    try { supportUnreadCount = supportDAO.unreadForBackOffice(role, accountId(user)); }
                    catch (Exception ignored) { supportUnreadCount = 0; }
                }
            }
            req.setAttribute("supportUnreadCount", supportUnreadCount);
            int staffNewOrderCount = 0;
            int deliveryActiveOrderCount = 0;
            int accountNotificationCount = 0;
            if (user != null) {
                String role = String.valueOf(user.get("vaiTro"));
                int accountId = accountId(user);
                try {
                    if ("STAFF".equals(role)) staffNewOrderCount = commonOrderDAO.staffNewOrderCount(accountId);
                    if ("DELIVERY".equals(role)) deliveryActiveOrderCount = commonOrderDAO.deliveryActiveOrderCount(accountId);
                    if ("ADMIN".equals(role) || "STAFF".equals(role) || "DELIVERY".equals(role)) {
                        accountNotificationCount = commonNotificationDAO.unreadCount(accountId);
                    }
                } catch (Exception ignored) {
                    staffNewOrderCount = 0;
                    deliveryActiveOrderCount = 0;
                    accountNotificationCount = 0;
                }
            }
            req.setAttribute("staffNewOrderCount", staffNewOrderCount);
            req.setAttribute("deliveryActiveOrderCount", deliveryActiveOrderCount);
            req.setAttribute("accountNotificationCount", accountNotificationCount);
            try {
                req.setAttribute("headerCategories", categoryDAO.categories(true));
            } catch (Exception ignored) {
                req.setAttribute("headerCategories", java.util.Collections.emptyList());
            }
            req.setAttribute("activePath", req.getServletPath());
            req.setAttribute("shopHotline", AppConfig.get("shop.hotline", "0355122472"));
            req.setAttribute("shopEmail", AppConfig.get("shop.email", "hello@celineclosset.vn"));
            req.setAttribute("shopAddress", AppConfig.get("shop.address", "Phan Thiết, Bình Thuận"));
            req.setAttribute("shopFacebook", AppConfig.get("shop.facebook", "#"));
            req.setAttribute("shopInstagram", AppConfig.get("shop.instagram", "#"));
            req.setAttribute("shopTiktok", AppConfig.get("shop.tiktok", "#"));
            req.setAttribute("shopYoutube", AppConfig.get("shop.youtube", "#"));
            req.setAttribute("shopBankName", AppConfig.get("shop.bankName", "TPBank"));
            req.setAttribute("shopBankCode", AppConfig.get("shop.bankCode", "970423"));
            req.setAttribute("shopBankAccount", AppConfig.get("shop.bankAccount", ""));
            req.setAttribute("shopBankOwner", AppConfig.get("shop.bankOwner", ""));
            req.setAttribute("shopQrImage", AppConfig.get("shop.qrImage", "assets/images/payment/shop-qr-demo.png"));
            req.setAttribute("chatApiUrl", AppConfig.get("chat.api.url", ""));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> auth(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("auth");
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    /**
     * Đọc lại tài khoản từ database cho mỗi request cần đăng nhập.
     * Việc này ngăn phiên JSESSIONID cũ tiếp tục giữ maTK không còn tồn tại
     * sau khi database được tạo lại hoặc tài khoản bị xóa/khóa.
     */
    protected Map<String, Object> refreshAuthenticatedUser(HttpServletRequest req) throws Exception {
        Map<String, Object> current = auth(req);
        if (current == null) {
            return null;
        }

        Integer id = nullableAccountId(current);
        if (id == null) {
            invalidateSession(req);
            return null;
        }

        Map<String, Object> refreshed = accountDAO.accountById(id);
        if (refreshed == null) {
            invalidateSession(req);
            return null;
        }

        req.getSession().setAttribute("auth", refreshed);
        return refreshed;
    }

    protected int authId(HttpServletRequest req) {
        Map<String, Object> user = auth(req);
        Integer id = nullableAccountId(user);
        if (id == null) {
            throw new IllegalStateException("Phiên đăng nhập không còn hợp lệ. Vui lòng đăng nhập lại.");
        }
        return id;
    }

    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (refreshAuthenticatedUser(req) == null) {
                redirectToLogin(req, resp, true);
                return false;
            }
            return true;
        } catch (Exception e) {
            invalidateSession(req);
            redirectToLogin(req, resp, true);
            return false;
        }
    }

    protected boolean requireCustomer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> user = refreshAuthenticatedUser(req);
            if (user == null) {
                redirectToLogin(req, resp, true);
                return false;
            }
            if (!"CUSTOMER".equals(String.valueOf(user.get("vaiTro")))) {
                resp.sendRedirect(adminStartUrl(req));
                return false;
            }
            return true;
        } catch (Exception e) {
            invalidateSession(req);
            redirectToLogin(req, resp, true);
            return false;
        }
    }

    protected boolean requireBackOffice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> user = refreshAuthenticatedUser(req);
            if (user == null || !isBackOffice(user)) {
                redirectToLogin(req, resp, user == null);
                return false;
            }
            return true;
        } catch (Exception e) {
            invalidateSession(req);
            redirectToLogin(req, resp, true);
            return false;
        }
    }

    protected boolean requireOwnerRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> user = refreshAuthenticatedUser(req);
            if (user == null) {
                redirectToLogin(req, resp, true);
                return false;
            }
            if (!"ADMIN".equals(String.valueOf(user.get("vaiTro")))) {
                resp.sendRedirect(adminStartUrl(req));
                return false;
            }
            return true;
        } catch (Exception e) {
            invalidateSession(req);
            redirectToLogin(req, resp, true);
            return false;
        }
    }

    protected boolean requireOrderRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return requireBackOffice(req, resp);
    }

    protected boolean isBackOffice(Map<String, Object> user) {
        String role = String.valueOf(user.get("vaiTro"));
        return "ADMIN".equals(role) || "STAFF".equals(role) || "DELIVERY".equals(role);
    }

    protected String currentRole(HttpServletRequest req) {
        Map<String, Object> user = auth(req);
        return user == null ? "" : String.valueOf(user.get("vaiTro"));
    }

    protected String adminStartUrl(HttpServletRequest req) {
        return req.getContextPath() + adminStartPath(auth(req));
    }

    protected String adminStartPath(Map<String, Object> user) {
        if (user == null) {
            return "/login";
        }
        String role = String.valueOf(user.get("vaiTro"));
        if ("ADMIN".equals(role)) {
            return "/admin/dashboard";
        }
        if ("STAFF".equals(role)) {
            return "/admin/orders";
        }
        if ("DELIVERY".equals(role)) {
            return "/admin/order-tracking";
        }
        return "/home";
    }

    protected String firstNonBlank(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first.trim();
    }

    private int accountId(Map<String, Object> user) {
        Integer id = nullableAccountId(user);
        if (id == null) {
            throw new IllegalStateException("Tài khoản đăng nhập không có mã hợp lệ.");
        }
        return id;
    }

    private Integer nullableAccountId(Map<String, Object> user) {
        if (user == null) {
            return null;
        }
        Object value = user.get("maTK");
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private void invalidateSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp, boolean expired) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login" + (expired ? "?session=expired" : ""));
    }
}
