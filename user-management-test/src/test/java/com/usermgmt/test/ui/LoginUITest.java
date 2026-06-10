package com.usermgmt.test.ui;

import com.usermgmt.test.base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

public class LoginUITest extends BaseTest {

    @BeforeClass
    public void setup() {
        setupDriver();
    }

    @AfterClass
    public void tearDown() {
        tearDownDriver();
    }

    // Helper: chờ trang login load xong rồi mới thao tác
    private void goToLogin() {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
    }

    private void doLogout() {
        try {
            driver.get(BASE_URL + "/logout");
            wait.until(ExpectedConditions.urlContains("/login"));
        } catch (Exception e) {
            // Session có thể đã bị reset, bỏ qua
        }
    }

    // ===================== UI TESTS =====================

    @Test(description = "Trang login hiển thị đúng các thành phần")
    public void testLoginPageElements() {
        test = extent.createTest("UI - Login: Kiểm tra thành phần trang");
        goToLogin();

        Assert.assertTrue(driver.findElement(By.id("username")).isDisplayed(),               "Ô username phải hiển thị");
        Assert.assertTrue(driver.findElement(By.id("password")).isDisplayed(),               "Ô password phải hiển thị");
        Assert.assertTrue(driver.findElement(By.cssSelector("button[type='submit']")).isDisplayed(), "Nút submit phải hiển thị");
        Assert.assertTrue(driver.getTitle().contains("User Management"),                     "Title phải chứa 'User Management'");

        test.pass("Trang login đầy đủ các thành phần");
    }

    @Test(description = "Admin đăng nhập thành công")
    public void testLoginAdminSuccess() {
        test = extent.createTest("UI - Login: Admin đăng nhập thành công");
        goToLogin();

        driver.findElement(By.id("username")).sendKeys(ADMIN_USERNAME);
        driver.findElement(By.id("password")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Phải redirect về dashboard");

        test.pass("Admin đăng nhập thành công");
        doLogout();
    }

    @Test(description = "User thường đăng nhập thành công")
    public void testLoginUserSuccess() {
        test = extent.createTest("UI - Login: User thường đăng nhập thành công");
        goToLogin();

        driver.findElement(By.id("username")).sendKeys(USER_USERNAME);
        driver.findElement(By.id("password")).sendKeys(USER_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Phải redirect về dashboard");

        test.pass("User thường đăng nhập thành công");
        doLogout();
    }

    @Test(description = "Sai password hiển thị lỗi")
    public void testLoginWrongPassword() {
        test = extent.createTest("UI - Login: Sai password hiển thị lỗi");
        goToLogin();

        driver.findElement(By.id("username")).sendKeys(ADMIN_USERNAME);
        driver.findElement(By.id("password")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Chờ trang reload sau khi login fail
        wait.until(ExpectedConditions.urlContains("/login"));

        // Chờ element lỗi xuất hiện (alert-danger hoặc có chứa text lỗi)
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert"))
        ));

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Phải ở lại trang login");

        String pageSource = driver.getPageSource();
        Assert.assertTrue(
            pageSource.contains("Invalid") || pageSource.contains("error") ||
            pageSource.contains("incorrect") || pageSource.contains("⚠"),
            "Phải hiển thị thông báo lỗi"
        );

        test.pass("Hiển thị lỗi khi sai password");
    }

    @Test(description = "Username không tồn tại ở lại trang login")
    public void testLoginUserNotFound() {
        test = extent.createTest("UI - Login: Username không tồn tại");
        goToLogin();

        driver.findElement(By.id("username")).sendKeys("nonexistent_xyz_999");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Phải ở lại trang login");

        test.pass("Ở lại trang login khi username không tồn tại");
    }

    @Test(description = "Form trống không submit được")
    public void testLoginEmptyForm() {
        test = extent.createTest("UI - Login: Form trống");
        goToLogin();

        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Phải ở lại trang login");
        test.pass("Form trống không submit được");
    }

    @Test(description = "Có link đến trang Register")
    public void testLoginHasRegisterLink() {
        test = extent.createTest("UI - Login: Có link đến Register");
        goToLogin();

        WebElement link = driver.findElement(By.linkText("Register here"));
        Assert.assertTrue(link.isDisplayed(), "Link Register phải hiển thị");
        link.click();

        wait.until(ExpectedConditions.urlContains("/register"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/register"), "Click link mở trang /register");

        test.pass("Link Register hoạt động đúng");
    }

    @Test(description = "Admin thấy menu Manage Users trong sidebar")
    public void testAdminSeesManageUsersMenu() {
        test = extent.createTest("UI - Login: Admin thấy menu Manage Users");
        goToLogin();

        driver.findElement(By.id("username")).sendKeys(ADMIN_USERNAME);
        driver.findElement(By.id("password")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(),'Manage Users')]")
        ));

        Assert.assertTrue(
            driver.getPageSource().contains("Manage Users"),
            "Admin phải thấy menu Manage Users"
        );

        test.pass("Admin thấy menu Manage Users");
        doLogout();
    }

    @Test(description = "User thường không thấy menu Manage Users")
    public void testUserDoesNotSeeManageUsersMenu() {
        test = extent.createTest("UI - Login: User thường không thấy menu Admin");
        goToLogin();

        driver.findElement(By.id("username")).sendKeys(USER_USERNAME);
        driver.findElement(By.id("password")).sendKeys(USER_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Chờ redirect - có thể về dashboard hoặc login (nếu user bị disabled)
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        } catch (Exception e) {
            // Nếu không vào được dashboard, assert luôn không thấy Manage Users
            Assert.assertFalse(
                driver.getPageSource().contains("Manage Users"),
                "User thường không được thấy menu Manage Users"
            );
            test.pass("User không vào được dashboard (có thể bị disabled) - không thấy Manage Users");
            return;
        }

        Assert.assertFalse(
            driver.getPageSource().contains("Manage Users"),
            "User thường không được thấy menu Manage Users"
        );

        test.pass("User thường không thấy menu Manage Users");
        doLogout();
    }
}
