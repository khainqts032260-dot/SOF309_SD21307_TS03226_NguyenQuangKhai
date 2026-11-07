package vn.celineclosset.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import vn.celineclosset.dao.SupportDAO;
import vn.celineclosset.dao.PaymentDAO;
import vn.celineclosset.dao.OrderDAO;
import vn.celineclosset.dao.DemoDataDAO;
import vn.celineclosset.dao.NewsDAO;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý tài nguyên dùng chung của ứng dụng và dọn chat hỗ trợ hết hạn.
 */
@WebListener
public class JpaContextListener implements ServletContextListener {
    private ScheduledExecutorService supportCleanupExecutor;
    private ScheduledExecutorService paymentExpirationExecutor;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        if (AppConfig.getBoolean("app.devMode", true)) {
            DemoDataDAO demoDataDAO = new DemoDataDAO();
            try {
                int assignments = demoDataDAO.repairLegacyAssignments();
                event.getServletContext().log("Demo data: repaired legacy STAFF/DELIVERY assignments=" + assignments + ".");
            } catch (Exception exception) {
                event.getServletContext().log("Chưa thể sửa phân công đơn mẫu: " + exception.getMessage(), exception);
            }
            try {
                int sales = demoDataDAO.ensureDemoSalesForUnsoldProducts();
                event.getServletContext().log("Demo data: added product-sale orders=" + sales + ".");
            } catch (Exception exception) {
                event.getServletContext().log("Chưa thể bổ sung lượt mua mẫu: " + exception.getMessage(), exception);
            }
            try {
                int support = demoDataDAO.repairSupportDemoData();
                event.getServletContext().log("Demo data: support repairs=" + support + ".");
            } catch (Exception exception) {
                event.getServletContext().log("Chưa thể sửa dữ liệu chat mẫu: " + exception.getMessage(), exception);
            }
            try {
                new NewsDAO().ensureDemoNews();
            } catch (Exception exception) {
                event.getServletContext().log("Chưa thể bổ sung nội dung tin tức mẫu: " + exception.getMessage(), exception);
            }
        }

        try {
            int repaired = new OrderDAO().normalizeDemoTrackingCoordinates();
            if (repaired > 0) {
                event.getServletContext().log("Đã sửa " + repaired
                        + " tọa độ GPS mẫu để xe nằm trên hành lang giao hàng tại Việt Nam.");
            }
        } catch (Exception exception) {
            event.getServletContext().log("Chưa thể chuẩn hóa tọa độ bản đồ mẫu: "
                    + exception.getMessage(), exception);
        }

        int inactiveHours = Math.max(1,
                AppConfig.getInt("support.chat.autoDeleteHours", 48));
        int intervalMinutes = Math.max(5,
                AppConfig.getInt("support.chat.cleanupIntervalMinutes", 60));

        supportCleanupExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "celine-support-chat-cleanup");
            thread.setDaemon(true);
            return thread;
        });


        int paymentExpirationMinutes = Math.max(1,
                AppConfig.getInt("payment.expirationMinutes", 10));
        int paymentCheckSeconds = Math.max(15,
                AppConfig.getInt("payment.expirationCheckSeconds", 30));

        paymentExpirationExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "celine-bank-payment-expiration");
            thread.setDaemon(true);
            return thread;
        });

        paymentExpirationExecutor.scheduleWithFixedDelay(() -> {
            try {
                PaymentDAO paymentDAO = new PaymentDAO();
                int normalized = paymentDAO.normalizeBankPaymentCodes();
                int expired = paymentDAO.expirePendingBankPayments(paymentExpirationMinutes);
                if (normalized > 0) {
                    event.getServletContext().log("Đã chuẩn hóa " + normalized
                            + " mã thanh toán BANK sang dạng có số 0 phía trước.");
                }
                if (expired > 0) {
                    event.getServletContext().log("Đã xử lý và ẩn khỏi lịch sử khách " + expired
                            + " đơn chuyển khoản chưa thanh toán sau "
                            + paymentExpirationMinutes + " phút.");
                }
            } catch (Exception exception) {
                event.getServletContext().log(
                        "Không thể chạy tác vụ kiểm tra thanh toán hết hạn: "
                                + exception.getMessage(), exception);
            }
        }, 5, paymentCheckSeconds, TimeUnit.SECONDS);

        supportCleanupExecutor.scheduleWithFixedDelay(() -> {
            try {
                int deleted = new SupportDAO().deleteInactiveConversations(inactiveHours);
                if (deleted > 0) {
                    event.getServletContext().log(
                            "Đã tự động xóa " + deleted
                                    + " cuộc trò chuyện hỗ trợ không hoạt động quá "
                                    + inactiveHours + " giờ.");
                }
            } catch (Exception exception) {
                // Không làm ứng dụng dừng nếu database tạm thời chưa sẵn sàng.
                event.getServletContext().log(
                        "Không thể chạy tác vụ dọn chat hỗ trợ: "
                                + exception.getMessage(), exception);
            }
        }, 1, intervalMinutes, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (supportCleanupExecutor != null) {
            supportCleanupExecutor.shutdownNow();
        }
        if (paymentExpirationExecutor != null) {
            paymentExpirationExecutor.shutdownNow();
        }
        JPAUtil.close();
    }
}
