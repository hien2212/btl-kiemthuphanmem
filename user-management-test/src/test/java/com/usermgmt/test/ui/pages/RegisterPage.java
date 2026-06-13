package com.usermgmt.test.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model – trang /register.
 */
public class RegisterPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // ===== Locators =====
    private final By usernameInput       = By.id("username");
    private final By emailInput          = By.id("email");
    private final By passwordInput       = By.id("password");
    private final By confirmPasswordInput = By.id("confirmPassword");
    private final By submitButton        = By.cssSelector("button[type='submit']");
    private final By signInLink          = By.linkText("Sign in");
    private final By errorMessage        = By.cssSelector(".alert-danger, .alert");
    private final By successMessage      = By.xpath(
        "//*[contains(text(),'successful') or contains(text(),'success') or contains(text(),'Registration')]"
    );

    public RegisterPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    // ===== Navigation =====
    public RegisterPage open(String baseUrl) {
        driver.get(baseUrl + "/register");
        wait.until(ExpectedConditions.presenceOfElementLocated(usernameInput));
        return this;
    }

    // ===== Actions =====
    public RegisterPage enterUsername(String username) {
        WebElement el = driver.findElement(usernameInput);
        el.clear();
        el.sendKeys(username);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        WebElement el = driver.findElement(emailInput);
        el.clear();
        el.sendKeys(email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        WebElement el = driver.findElement(passwordInput);
        el.clear();
        el.sendKeys(password);
        return this;
    }

    public RegisterPage enterConfirmPassword(String confirmPassword) {
        WebElement el = driver.findElement(confirmPasswordInput);
        el.clear();
        el.sendKeys(confirmPassword);
        return this;
    }

    public void clickSubmit() {
        driver.findElement(submitButton).click();
    }

    /** Điền đầy đủ form và submit. */
    public void register(String username, String email, String password) {
        enterUsername(username);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(password);
        clickSubmit();
    }

    public void clickSignInLink() {
        driver.findElement(signInLink).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    // ===== Queries =====
    public boolean isUsernameDisplayed()        { return driver.findElement(usernameInput).isDisplayed(); }
    public boolean isEmailDisplayed()           { return driver.findElement(emailInput).isDisplayed(); }
    public boolean isPasswordDisplayed()        { return driver.findElement(passwordInput).isDisplayed(); }
    public boolean isConfirmPasswordDisplayed() { return driver.findElement(confirmPasswordInput).isDisplayed(); }
    public boolean isSubmitDisplayed()          { return driver.findElement(submitButton).isDisplayed(); }
    public boolean isSignInLinkDisplayed()      { return driver.findElement(signInLink).isDisplayed(); }

    public boolean isOnRegisterPage() { return driver.getCurrentUrl().contains("/register"); }
    public boolean isOnLoginPage()    { return driver.getCurrentUrl().contains("/login"); }

    public boolean isErrorDisplayed() {
        try {
            return driver.findElement(errorMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuccessDisplayed() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(successMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getPageSource() { return driver.getPageSource(); }
}
