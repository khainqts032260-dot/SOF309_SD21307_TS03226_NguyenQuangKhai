package com.poly.sof309.ui.tests;

import com.poly.sof309.ui.pages.LoginPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

/**
 * =========================================================================
 * [Y4] KỊCH BẢN CHẠY SELENIUM TEST TRÊN TRÌNH DUYỆT THỰC TẾ
 * Hỗ trợ cả chế độ có giao diện (Local) và chế độ Headless (GitHub Actions).
 * =========================================================================
 */
public class LoginTest {
    private WebDriver driver;
    private LoginPage loginPage;
    private final String BASE_URL = "http://localhost:8080/login"; // URL web của sinh viên

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Kiểm tra nếu đang chạy trên CI/CD GitHub Actions thì tự bật Headless
        if (Boolean.parseBoolean(System.getenv("CI"))) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }
        
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        loginPage = new LoginPage(driver);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Test giao diện đăng nhập thành công")
    @Disabled("Bỏ comment @Disabled khi đã khởi động web ở local để chạy thật")
    public void testLoginSuccessUI() {
        driver.get(BASE_URL);
        loginPage.login("admin", "123");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home") || driver.getCurrentUrl().contains("/dashboard"),
                "Sau khi đăng nhập thành công phải chuyển về trang chủ");
    }
}

