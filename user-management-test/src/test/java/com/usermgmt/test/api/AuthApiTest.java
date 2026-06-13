package com.usermgmt.test.api;

import com.usermgmt.test.base.BaseTest;
import io.restassured.http.ContentType;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthApiTest extends BaseTest {

    @BeforeClass
    public void setup() {
        setupRestAssured();
    }

    // ===================== DATA PROVIDERS =====================

    /**
     * DataProvider: các trường hợp đăng ký không hợp lệ.
     * Mỗi hàng: { username, email, password, confirmPassword, expectedStatus, mô tả }
     */
    @DataProvider(name = "invalidRegisterData")
    public Object[][] invalidRegisterData() {
        return new Object[][] {
            // password không khớp
            { randomUsername(), randomEmail(), "password123", "different456",  400, "Password không khớp" },
            // email sai định dạng
            { randomUsername(), "not-an-email", "password123", "password123", 400, "Email không hợp lệ" },
            // password quá ngắn
            { randomUsername(), randomEmail(),  "123",         "123",          400, "Password quá ngắn (<6 ký tự)" },
            // body rỗng (thiếu field)
            { "",               "",             "",            "",             400, "Thiếu field bắt buộc" },
        };
    }

    /**
     * DataProvider: các trường hợp đăng nhập thất bại.
     * Mỗi hàng: { username, password, expectedStatus, mô tả }
     */
    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() {
        return new Object[][] {
            { ADMIN_USERNAME,        "wrongpassword",  401, "Sai password" },
            { "nonexistentuser_xyz", "password123",    401, "Username không tồn tại" },
            { "",                    "",               400, "Username và password rỗng" },
        };
    }

    // ===================== REGISTER =====================

    @Test(description = "Đăng ký thành công với thông tin hợp lệ")
    public void testRegisterSuccess() {
        test = extent.createTest("API - Register: Thành công");

        Map<String, String> body = new HashMap<>();
        body.put("username", randomUsername());
        body.put("email", randomEmail());
        body.put("password", "password123");
        body.put("confirmPassword", "password123");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(200)
            .body("username", notNullValue())
            .body("role", equalTo("ROLE_USER"))
            .body("id", notNullValue());

        test.pass("Đăng ký thành công, trả về thông tin user với role ROLE_USER");
    }

    @Test(description = "Đăng ký thất bại khi username đã tồn tại")
    public void testRegisterDuplicateUsername() {
        test = extent.createTest("API - Register: Username đã tồn tại");

        Map<String, String> body = new HashMap<>();
        body.put("username", ADMIN_USERNAME);
        body.put("email", randomEmail());
        body.put("password", "password123");
        body.put("confirmPassword", "password123");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message", containsString("Username already exists"))
            .body("success", equalTo(false));

        test.pass("Trả về 400 khi username đã tồn tại");
    }

    @Test(description = "Đăng ký thất bại khi email đã tồn tại")
    public void testRegisterDuplicateEmail() {
        test = extent.createTest("API - Register: Email đã tồn tại");

        Map<String, String> body = new HashMap<>();
        body.put("username", randomUsername());
        body.put("email", "admin@example.com");
        body.put("password", "password123");
        body.put("confirmPassword", "password123");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message", containsString("Email already exists"));

        test.pass("Trả về 400 khi email đã tồn tại");
    }

    /**
     * Data-driven test: kiểm thử nhiều trường hợp đăng ký không hợp lệ
     * chỉ với một method duy nhất nhờ @DataProvider.
     */
    @Test(
        description   = "Đăng ký thất bại – data-driven (nhiều kịch bản)",
        dataProvider  = "invalidRegisterData"
    )
    public void testRegisterInvalidData(
            String username, String email,
            String password, String confirmPassword,
            int expectedStatus, String scenario) {

        test = extent.createTest("API - Register (data-driven): " + scenario);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("confirmPassword", confirmPassword);

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(expectedStatus);

        test.pass("Trả về " + expectedStatus + " – kịch bản: " + scenario);
    }

    // ===================== LOGIN =====================

    @Test(description = "Đăng nhập thành công với admin")
    public void testLoginAdminSuccess() {
        test = extent.createTest("API - Login: Admin thành công");

        Map<String, String> body = new HashMap<>();
        body.put("username", ADMIN_USERNAME);
        body.put("password", ADMIN_PASSWORD);

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token",    notNullValue())
            .body("type",     equalTo("Bearer"))
            .body("username", equalTo(ADMIN_USERNAME))
            .body("role",     equalTo("ROLE_ADMIN"));

        test.pass("Đăng nhập admin thành công, nhận được JWT token");
    }

    @Test(description = "Đăng nhập thành công với user thường")
    public void testLoginUserSuccess() {
        test = extent.createTest("API - Login: User thường thành công");

        Map<String, String> body = new HashMap<>();
        body.put("username", USER_USERNAME);
        body.put("password", USER_PASSWORD);

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token",    notNullValue())
            .body("role",     equalTo("ROLE_USER"))
            .body("username", equalTo(USER_USERNAME));

        test.pass("Đăng nhập user thường thành công");
    }

    /**
     * Data-driven test: nhiều kịch bản đăng nhập thất bại.
     */
    @Test(
        description  = "Đăng nhập thất bại – data-driven (nhiều kịch bản)",
        dataProvider = "invalidLoginData"
    )
    public void testLoginInvalidData(
            String username, String password,
            int expectedStatus, String scenario) {

        test = extent.createTest("API - Login (data-driven): " + scenario);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(expectedStatus);

        test.pass("Trả về " + expectedStatus + " – kịch bản: " + scenario);
    }

    @Test(description = "Response login có đầy đủ các field")
    public void testLoginResponseStructure() {
        test = extent.createTest("API - Login: Kiểm tra cấu trúc response");

        Map<String, String> body = new HashMap<>();
        body.put("username", ADMIN_USERNAME);
        body.put("password", ADMIN_PASSWORD);

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token",    notNullValue())
            .body("type",     notNullValue())
            .body("id",       notNullValue())
            .body("username", notNullValue())
            .body("email",    notNullValue())
            .body("role",     notNullValue());

        test.pass("Response chứa đầy đủ: token, type, id, username, email, role");
    }
}
