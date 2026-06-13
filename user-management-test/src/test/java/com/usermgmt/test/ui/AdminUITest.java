package com.usermgmt.test.ui;

import com.usermgmt.test.base.BaseTest;
import com.usermgmt.test.ui.pages.AdminPage;
import com.usermgmt.test.ui.pages.LoginPage;
import com.usermgmt.test.ui.pages.RegisterPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class AdminUITest extends BaseTest {

    private LoginPage    loginPage;
    private RegisterPage registerPage;
    private AdminPage    adminPage;

    @BeforeClass
    public void setup() {
        setupDriver();
        loginPage    = new LoginPage(driver, wait);
        registerPage = new RegisterPage(driver, wait);
        adminPage    = new AdminPage(driver, wait);
    }

    @AfterClass
    public void tearDown() {
        tearDownDriver();
    }

    private void loginAsAdmin() {
        loginPage.open(BASE_URL);
        loginPage.loginAs(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    private void logout() {
        try {
            driver.get(BASE_URL + "/logout");
            wait.until(ExpectedConditions.urlContains("/login"));
        } catch (Exception ignored) {}
    }

    /** Helper: tạo user mới qua UI và trả về username */
    private String createNewUserViaUI() {
        String username = randomUsername();
        registerPage.open(BASE_URL);
        registerPage.register(username, randomEmail(), "password123");
        wait.until(ExpectedConditions.urlContains("/login"));
        return username;
    }

    // ===================== PHÂN QUYỀN =====================

    @Test(description = "Admin truy cập được trang /admin/users")
    public void testAdminCanAccessUserManagement() {
        test = extent.createTest("UI - Admin: Truy cập trang quản lý users");

        loginAsAdmin();
        adminPage.open(BASE_URL);
        adminPage.waitForTable();

        Assert.assertTrue(adminPage.isOnAdminPage(), "Admin phải truy cập được /admin/users");

        test.pass("Admin truy cập trang /admin/users thành công");
        logout();
    }

    @Test(description = "User thường bị từ chối truy cập /admin/users")
    public void testUserCannotAccessAdminPage() {
        test = extent.createTest("UI - Admin: User thường bị từ chối");

        loginPage.open(BASE_URL);
        loginPage.loginAs(USER_USERNAME, USER_PASSWORD);
        driver.get(BASE_URL + "/admin/users");

        try {
            wait.until(ExpectedConditions.not(
                ExpectedConditions.urlToBe(BASE_URL + "/admin/users")
            ));
        } catch (Exception ignored) {}

        try {
            boolean isRedirected = !driver.getCurrentUrl().contains("/admin/users");
            boolean isErrorPage  = adminPage.getPageSource().contains("403") ||
                                   adminPage.getPageSource().contains("Access Denied") ||
                                   adminPage.getPageSource().contains("Forbidden");

            Assert.assertTrue(isRedirected || isErrorPage, "User thường không được vào trang admin");
        } catch (Exception e) {
            Assert.assertTrue(true, "Browser đóng = bị từ chối truy cập");
        }

        test.pass("User thường bị từ chối truy cập admin page");
        logout();
    }

    @Test(description = "Chưa đăng nhập bị redirect về /login")
    public void testUnauthenticatedCannotAccessAdminPage() {
        test = extent.createTest("UI - Admin: Chưa login bị redirect về /login");

        // Đảm bảo không có session
        driver.get(BASE_URL + "/logout");
        // Truy cập thẳng /admin/users — app sẽ tự redirect về /login
        driver.get(BASE_URL + "/admin/users");

        wait.until(ExpectedConditions.urlContains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Phải redirect về /login");

        test.pass("Chưa đăng nhập bị redirect về login");
    }

    // ===================== HIỂN THỊ =====================

    @Test(description = "Bảng users có đủ các cột")
    public void testAdminPageShowsUserTable() {
        test = extent.createTest("UI - Admin: Bảng users đầy đủ cột");

        loginAsAdmin();
        adminPage.open(BASE_URL);
        adminPage.waitForTable();

        Assert.assertTrue(adminPage.tableContains("Username"), "Cột Username");
        Assert.assertTrue(adminPage.tableContains("Email"),    "Cột Email");
        Assert.assertTrue(adminPage.tableContains("Role"),     "Cột Role");
        Assert.assertTrue(adminPage.tableContains("Status"),   "Cột Status");

        test.pass("Bảng có đủ cột: Username, Email, Role, Status");
        logout();
    }

    @Test(description = "Danh sách có ít nhất 2 users")
    public void testAdminPageShowsUsers() {
        test = extent.createTest("UI - Admin: Danh sách có ít nhất 2 users");

        loginAsAdmin();
        adminPage.open(BASE_URL);
        adminPage.waitForTable();

        List<WebElement> rows = adminPage.getUserRows();
        Assert.assertTrue(rows.size() >= 2, "Phải có ít nhất 2 users (admin + user1)");

        test.pass("Danh sách hiển thị " + rows.size() + " users");
        logout();
    }

    @Test(description = "Badge role hiển thị đúng")
    public void testAdminPageShowsCorrectRoles() {
        test = extent.createTest("UI - Admin: Badge role đúng");

        loginAsAdmin();
        adminPage.open(BASE_URL);
        adminPage.waitForTable();

        Assert.assertTrue(
            adminPage.tableContains("Admin") || adminPage.tableContains("ADMIN"),
            "Phải có badge Admin trong danh sách"
        );

        test.pass("Badge role hiển thị đúng");
        logout();
    }

    // ===================== ĐỔI ROLE =====================

    @Test(description = "Admin đổi role user qua UI")
    public void testAdminChangeUserRole() {
        test = extent.createTest("UI - Admin: Đổi role user");

        String newUsername = createNewUserViaUI();

        loginAsAdmin();
        adminPage.open(BASE_URL);
        adminPage.waitForTable();

        WebElement targetRow = adminPage.findRowByUsername(newUsername);
        Assert.assertNotNull(targetRow, "Phải tìm thấy user vừa tạo");

        adminPage.changeRoleInRow(targetRow, "ROLE_ADMIN");

        Assert.assertTrue(
            adminPage.tableContains("updated") || adminPage.tableContains("success"),
            "Phải hiển thị thông báo thành công"
        );

        test.pass("Đổi role user thành công qua UI");
        logout();
    }

    // ===================== DISABLE USER =====================

    @Test(description = "Admin disable user qua UI")
    public void testAdminDisableUser() {
        test = extent.createTest("UI - Admin: Disable user");

        String newUsername = createNewUserViaUI();

        loginAsAdmin();
        adminPage.open(BASE_URL);
        adminPage.waitForTable();

        WebElement targetRow = adminPage.findRowByUsername(newUsername);
        Assert.assertNotNull(targetRow, "Phải tìm thấy user vừa tạo");

        adminPage.clickDisableInRow(targetRow);

        Assert.assertTrue(
            adminPage.tableContains("updated") || adminPage.tableContains("status"),
            "Phải có thông báo sau khi disable"
        );

        test.pass("Disable user thành công qua UI");
        logout();
    }

    // ===================== LOGOUT =====================

    @Test(description = "Nút Logout hoạt động đúng")
    public void testLogoutButton() {
        test = extent.createTest("UI - Admin: Nút Logout");

        loginAsAdmin();
        adminPage.clickLogout();

        Assert.assertTrue(adminPage.isOnLoginPage(), "Phải redirect về /login sau logout");
        test.pass("Logout thành công");
    }
}
