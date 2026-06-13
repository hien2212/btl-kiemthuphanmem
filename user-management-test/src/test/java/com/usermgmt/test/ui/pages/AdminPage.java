package com.usermgmt.test.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Page Object Model – trang /admin/users.
 */
public class AdminPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // ===== Locators =====
    private final By userTableRows  = By.cssSelector("tbody tr");
    private final By userTable      = By.cssSelector("table");
    private final By logoutButton   = By.cssSelector("button[type='submit']");

    public AdminPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    // ===== Navigation =====
    /**
     * Điều hướng đến /admin/users.
     * Không chờ URL vì nếu chưa login thì app redirect về /login.
     * Gọi waitForTable() sau nếu cần bảng hiển thị.
     */
    public AdminPage open(String baseUrl) {
        driver.get(baseUrl + "/admin/users");
        return this;
    }

    public void waitForTable() {
        wait.until(ExpectedConditions.presenceOfElementLocated(userTable));
    }

    // ===== Queries =====
    public boolean isOnAdminPage()  { return driver.getCurrentUrl().contains("/admin/users"); }
    public boolean isOnLoginPage()  { return driver.getCurrentUrl().contains("/login"); }

    public List<WebElement> getUserRows() {
        return driver.findElements(userTableRows);
    }

    public boolean tableContains(String text) {
        return driver.getPageSource().contains(text);
    }

    public String getPageSource() { return driver.getPageSource(); }

    // ===== Tìm row theo username =====
    public WebElement findRowByUsername(String username) {
        List<WebElement> rows = driver.findElements(userTableRows);
        for (WebElement row : rows) {
            if (row.getText().contains(username)) return row;
        }
        return null;
    }

    // ===== Actions =====
    public void changeRoleInRow(WebElement row, String role) {
        Select select = new Select(row.findElement(By.name("role")));
        select.selectByValue(role);
        row.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(userTableRows));
    }

    public void clickDisableInRow(WebElement row) {
        row.findElement(By.xpath(".//button[contains(text(),'Disable')]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(userTableRows));
    }

    public void clickLogout() {
        driver.findElement(logoutButton).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }
}
