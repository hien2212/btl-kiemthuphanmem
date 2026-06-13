package com.usermgmt.test.ui;

import com.usermgmt.test.base.BaseTest;
import com.usermgmt.test.ui.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.*;

public class LoginUITest extends BaseTest {

    private LoginPage loginPage;

    @BeforeClass
    public void setup() {
        setupDriver();
        loginPage = new LoginPage(driver, wait);
    }

    @AfterClass
    public void tearDown() {
        tearDownDriver();
    }

    private void logout() {
        try {
            driver.get(BASE_URL + "/logout");
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/login"));
        } catch (Exception ignored) {}
    }

    // ===================== UI TESTS =====================

    @Test(description = "Trang login hiển thị đúng các thành phần")
    public void testLoginPageElements() {
        test = extent.createTest("UI - Login: Kiểm tra thành phần trang");
        loginPage.open(BASE_URL);

        Assert.assertTrue(loginPage.isUsernameDisplayed(), "Ô username phải hiển thị");
        Assert.assertTrue(loginPage.isPasswordDisplayed(), "Ô password phải hiển thị");
        Assert.assertTrue(loginPage.isSubmitDisplayed(),   "Nút submit phải hiển thị");
        Assert.assertTrue(loginPage.getTitle().contains("User Management"), "Title phải chứa 'User Management'");

        test.pass("Trang login đầy đủ các thành phần");
    }

    @Test(description = "Admin đăng nhập thành công")
    public void testLoginAdminSuccess() {
        test = extent.createTest("UI - Login: Admin đăng nhập thành công");
        loginPage.open(BASE_URL);
        loginPage.loginAs(ADMIN_USERNAME, ADMIN_PASSWORD);

        Assert.assertTrue(loginPage.isOnDashboard(), "Phải redirect về dashboard");

        test.pass("Admin đăng nhập thành công");
        logout();
    }

    @Test(description = "User thường đăng nhập thành công")
    public void testLoginUserSuccess() {
        test = extent.createTest("UI - Login: User thường đăng nhập thành công");
        loginPage.open(BASE_URL);
        loginPage.loginAs(USER_USERNAME, USER_PASSWORD);

        Assert.assertTrue(loginPage.isOnDashboard(), "Phải redirect về dashboard");

        test.pass("User thường đăng nhập thành công");
        logout();
    }

    @Test(description = "Sai password hiển thị lỗi")
    public void testLoginWrongPassword() {
        test = extent.createTest("UI - Login: Sai password hiển thị lỗi");
        loginPage.open(BASE_URL)
                 .enterUsername(ADMIN_USERNAME)
                 .enterPassword("wrongpassword")
                 .clickSubmit();

        Assert.assertTrue(loginPage.isOnLoginPage(), "Phải ở lại trang login");
        Assert.assertTrue(loginPage.isErrorDisplayed(), "Phải hiển thị thông báo lỗi");

        test.pass("Hiển thị lỗi khi sai password");
    }

    @Test(description = "Username không tồn tại ở lại trang login")
    public void testLoginUserNotFound() {
        test = extent.createTest("UI - Login: Username không tồn tại");
        loginPage.open(BASE_URL)
                 .enterUsername("nonexistent_xyz_999")
                 .enterPassword("password123")
                 .clickSubmit();

        Assert.assertTrue(loginPage.isOnLoginPage(), "Phải ở lại trang login");

        test.pass("Ở lại trang login khi username không tồn tại");
    }

    @Test(description = "Form trống không submit được")
    public void testLoginEmptyForm() {
        test = extent.createTest("UI - Login: Form trống");
        loginPage.open(BASE_URL)
                 .enterUsername("")
                 .enterPassword("")
                 .clickSubmit();

        Assert.assertTrue(loginPage.isOnLoginPage(), "Phải ở lại trang login");
        test.pass("Form trống không submit được");
    }

    @Test(description = "Có link đến trang Register")
    public void testLoginHasRegisterLink() {
        test = extent.createTest("UI - Login: Có link đến Register");
        loginPage.open(BASE_URL);

        Assert.assertTrue(loginPage.isRegisterLinkDisplayed(), "Link Register phải hiển thị");
        loginPage.clickRegisterLink();

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"), "Click link mở trang /register");

        test.pass("Link Register hoạt động đúng");
    }

    @Test(description = "Admin thấy menu Manage Users trong sidebar")
    public void testAdminSeesManageUsersMenu() {
        test = extent.createTest("UI - Login: Admin thấy menu Manage Users");
        loginPage.open(BASE_URL);
        loginPage.loginAs(ADMIN_USERNAME, ADMIN_PASSWORD);

        Assert.assertTrue(loginPage.getPageSource().contains("Manage Users"),
            "Admin phải thấy menu Manage Users");

        test.pass("Admin thấy menu Manage Users");
        logout();
    }

    @Test(description = "User thường không thấy menu Manage Users")
    public void testUserDoesNotSeeManageUsersMenu() {
        test = extent.createTest("UI - Login: User thường không thấy menu Admin");
        loginPage.open(BASE_URL);

        try {
            loginPage.loginAs(USER_USERNAME, USER_PASSWORD);
        } catch (Exception e) {
            Assert.assertFalse(loginPage.getPageSource().contains("Manage Users"),
                "User thường không được thấy Manage Users");
            test.pass("User không vào được dashboard – không thấy Manage Users");
            return;
        }

        Assert.assertFalse(loginPage.getPageSource().contains("Manage Users"),
            "User thường không được thấy menu Manage Users");

        test.pass("User thường không thấy menu Manage Users");
        logout();
    }
}
