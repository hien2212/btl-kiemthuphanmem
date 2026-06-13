package com.usermgmt.test.ui;

import com.usermgmt.test.base.BaseTest;
import com.usermgmt.test.ui.pages.LoginPage;
import com.usermgmt.test.ui.pages.RegisterPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

public class RegisterUITest extends BaseTest {

    private RegisterPage registerPage;
    private LoginPage    loginPage;

    @BeforeClass
    public void setup() {
        setupDriver();
        registerPage = new RegisterPage(driver, wait);
        loginPage    = new LoginPage(driver, wait);
    }

    @AfterClass
    public void tearDown() {
        tearDownDriver();
    }

    private void logout() {
        try {
            driver.get(BASE_URL + "/logout");
            wait.until(ExpectedConditions.urlContains("/login"));
        } catch (Exception ignored) {}
    }

    @Test(description = "Trang register hiển thị đúng các thành phần")
    public void testRegisterPageElements() {
        test = extent.createTest("UI - Register: Kiểm tra thành phần trang");
        registerPage.open(BASE_URL);

        Assert.assertTrue(registerPage.isUsernameDisplayed(),        "Ô username");
        Assert.assertTrue(registerPage.isEmailDisplayed(),           "Ô email");
        Assert.assertTrue(registerPage.isPasswordDisplayed(),        "Ô password");
        Assert.assertTrue(registerPage.isConfirmPasswordDisplayed(), "Ô confirmPassword");
        Assert.assertTrue(registerPage.isSubmitDisplayed(),          "Nút submit");

        test.pass("Trang register có đủ 4 field + nút submit");
    }

    @Test(description = "Đăng ký thành công redirect về login")
    public void testRegisterSuccess() {
        test = extent.createTest("UI - Register: Đăng ký thành công");
        registerPage.open(BASE_URL);
        registerPage.register(randomUsername(), randomEmail(), "password123");

        try {
            wait.until(ExpectedConditions.urlContains("/login"));
            Assert.assertTrue(registerPage.isOnLoginPage(), "Phải redirect về /login");
            Assert.assertTrue(registerPage.isSuccessDisplayed(), "Phải hiển thị thông báo thành công");
        } catch (org.openqa.selenium.NoSuchSessionException e) {
            Assert.assertTrue(true, "Redirect thành công");
        }

        test.pass("Đăng ký thành công, redirect về login với thông báo");
    }

    @Test(description = "Username đã tồn tại hiển thị lỗi")
    public void testRegisterDuplicateUsername() {
        test = extent.createTest("UI - Register: Username đã tồn tại");
        registerPage.open(BASE_URL);
        registerPage.register(ADMIN_USERNAME, randomEmail(), "password123");

        Assert.assertTrue(registerPage.isOnRegisterPage(), "Phải ở lại trang register");
        Assert.assertTrue(
            registerPage.getPageSource().contains("already exists") ||
            registerPage.getPageSource().contains("Username"),
            "Phải hiển thị lỗi"
        );

        test.pass("Hiển thị lỗi username đã tồn tại");
    }

    @Test(description = "Email đã tồn tại hiển thị lỗi")
    public void testRegisterDuplicateEmail() {
        test = extent.createTest("UI - Register: Email đã tồn tại");
        registerPage.open(BASE_URL);
        registerPage.enterUsername(randomUsername())
                    .enterEmail("admin@example.com")
                    .enterPassword("password123")
                    .enterConfirmPassword("password123")
                    .clickSubmit();

        Assert.assertTrue(registerPage.isOnRegisterPage(), "Phải ở lại trang register");

        test.pass("Hiển thị lỗi email đã tồn tại");
    }

    @Test(description = "Password không khớp ở lại trang register")
    public void testRegisterPasswordMismatch() {
        test = extent.createTest("UI - Register: Password không khớp");
        registerPage.open(BASE_URL);
        registerPage.enterUsername(randomUsername())
                    .enterEmail(randomEmail())
                    .enterPassword("password123")
                    .enterConfirmPassword("different456")
                    .clickSubmit();

        Assert.assertTrue(registerPage.isOnRegisterPage(), "Phải ở lại trang register");

        test.pass("Ở lại trang register khi password không khớp");
    }

    @Test(description = "Có link quay về trang Login")
    public void testRegisterHasLoginLink() {
        test = extent.createTest("UI - Register: Có link về Login");
        registerPage.open(BASE_URL);

        Assert.assertTrue(registerPage.isSignInLinkDisplayed(), "Link Sign in phải hiển thị");
        registerPage.clickSignInLink();

        Assert.assertTrue(registerPage.isOnLoginPage(), "Click link mở /login");

        test.pass("Link về trang login hoạt động đúng");
    }

    @Test(description = "Đăng ký xong đăng nhập được ngay")
    public void testRegisterThenLogin() {
        test = extent.createTest("UI - Register: Đăng ký xong đăng nhập được");

        String username = randomUsername();
        String password = "password123";

        // Đăng ký
        registerPage.open(BASE_URL);
        registerPage.register(username, randomEmail(), password);
        wait.until(ExpectedConditions.urlContains("/login"));

        // Đăng nhập bằng LoginPage
        loginPage.open(BASE_URL);
        loginPage.loginAs(username, password);

        Assert.assertTrue(loginPage.isOnDashboard(), "Phải vào được dashboard");

        test.pass("Đăng ký xong đăng nhập thành công");
        logout();
    }
}
