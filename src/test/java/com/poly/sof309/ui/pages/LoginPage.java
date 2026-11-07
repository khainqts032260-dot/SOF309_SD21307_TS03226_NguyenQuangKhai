package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * =========================================================================
 * [Y4] MẪU PAGE OBJECT MODEL (POM) CHO TRANG ĐĂNG NHẬP
 * Class này chỉ quản lý các phần tử (Locator) và hành động trên trang Web.
 * =========================================================================
 */
public class LoginPage {
    private WebDriver driver;

    // 1. Định nghĩa các Locator trên trang đăng nhập của web cá nhân
    private By txtUsername = By.id("username");
    private By txtPassword = By.id("password");
    private By btnLogin = By.xpath("//button[@type='submit']");
    private By lblErrorMessage = By.id("error-message");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // 2. Các hành động nghiệp vụ
    public void enterUsername(String username) {
        driver.findElement(txtUsername).clear();
        driver.findElement(txtUsername).sendKeys(username);
    }

    public void enterPassword(String password) {
        driver.findElement(txtPassword).clear();
        driver.findElement(txtPassword).sendKeys(password);
    }

    public void clickLogin() {
        driver.findElement(btnLogin).click();
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public String getErrorMessage() {
        return driver.findElement(lblErrorMessage).getText();
    }
}

