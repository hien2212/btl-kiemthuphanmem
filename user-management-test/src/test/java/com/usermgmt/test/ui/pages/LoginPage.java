package com.usermgmt.test.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model – trang /login.
 * Tập trung toàn bộ selector và thao tác tại đây,
 * giúp test class sạch và dễ bảo trì khi UI thay đổi.
 */
public class LoginPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // ===== Locators =====
    private final By usernameInput  = By.id("username");
    private final By passwordInput  = By.id("password");
    private final By submitButton   = By.cssSelector("button[type='submit']");
    private final By registerLink   = By.linkText("Register here");
    private final By errorAlert     = By.cssSelector(".alert-danger, .alert");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    // ===== Navigation =====
    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(usernameInput));
        return this;
    }

    // ===== Actions =====
    public LoginPage enterUsername(String username) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        el.clear();
        el.sendKeys(username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
        return this;
    }

    public void clickSubmit() {
        driver.findElement(submitButton).click();
    }

    /** Thực hiện đăng nhập đầy đủ và chờ redirect. */
    public void loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickSubmit();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    public void clickRegisterLink() {
        driver.findElement(registerLink).click();
        wait.until(ExpectedConditions.urlContains("/register"));
    }

    // ===== Queries =====
    public boolean isUsernameDisplayed() {
        return driver.findElement(usernameInput).isDisplayed();
    }

    public boolean isPasswordDisplayed() {
        return driver.findElement(passwordInput).isDisplayed();
    }

    public boolean isSubmitDisplayed() {
        return driver.findElement(submitButton).isDisplayed();
    }

    public boolean isRegisterLinkDisplayed() {
        return driver.findElement(registerLink).isDisplayed();
    }

    public boolean isErrorDisplayed() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(errorAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/login");
    }

    public boolean isOnDashboard() {
        return driver.getCurrentUrl().contains("/dashboard");
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getPageSource() {
        return driver.getPageSource();
    }
}
