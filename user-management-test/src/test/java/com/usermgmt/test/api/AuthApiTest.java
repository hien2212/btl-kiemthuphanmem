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

    @Test(description = "Đăng ký thất bại khi password không khớp")
    public void testRegisterPasswordMismatch() {
        test = extent.createTest("API - Register: Password không khớp");

        Map<String, String> body = new HashMap<>();
        body.put("username", randomUsername());
        body.put("email", randomEmail());
        body.put("password", "password123");
        body.put("confirmPassword", "different456");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400);

        test.pass("Trả về 400 khi password không khớp");
    }

    @Test(description = "Đăng ký thất bại khi thiếu field bắt buộc")
    public void testRegisterMissingFields() {
        test = extent.createTest("API - Register: Thiếu field bắt buộc");

        // Gửi body rỗng hoàn toàn
        freshRequest()
            .body("{}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400);

        test.pass("Trả về 400 khi các field bắt buộc bị trống");
    }

    @Test(description = "Đăng ký thất bại khi email không đúng định dạng")
    public void testRegisterInvalidEmail() {
        test = extent.createTest("API - Register: Email không hợp lệ");

        Map<String, String> body = new HashMap<>();
        body.put("username", randomUsername());
        body.put("email", "not-an-email");
        body.put("password", "password123");
        body.put("confirmPassword", "password123");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400);

        test.pass("Trả về 400 khi email không đúng định dạng");
    }

    @Test(description = "Đăng ký thất bại khi password quá ngắn")
    public void testRegisterShortPassword() {
        test = extent.createTest("API - Register: Password quá ngắn");

        Map<String, String> body = new HashMap<>();
        body.put("username", randomUsername());
        body.put("email", randomEmail());
        body.put("password", "123");
        body.put("confirmPassword", "123");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400);

        test.pass("Trả về 400 khi password dưới 6 ký tự");
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
            .body("token", notNullValue())
            .body("type", equalTo("Bearer"))
            .body("username", equalTo(ADMIN_USERNAME))
            .body("role", equalTo("ROLE_ADMIN"));

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
            .body("token", notNullValue())
            .body("role", equalTo("ROLE_USER"))
            .body("username", equalTo(USER_USERNAME));

        test.pass("Đăng nhập user thường thành công");
    }

    @Test(description = "Đăng nhập thất bại với sai password")
    public void testLoginWrongPassword() {
        test = extent.createTest("API - Login: Sai password");

        Map<String, String> body = new HashMap<>();
        body.put("username", ADMIN_USERNAME);
        body.put("password", "wrongpassword");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("success", equalTo(false));

        test.pass("Trả về 401 khi sai password");
    }

    @Test(description = "Đăng nhập thất bại với username không tồn tại")
    public void testLoginUserNotFound() {
        test = extent.createTest("API - Login: Username không tồn tại");

        Map<String, String> body = new HashMap<>();
        body.put("username", "nonexistentuser_xyz");
        body.put("password", "password123");

        freshRequest()
            .body(body)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401);

        test.pass("Trả về 401 khi username không tồn tại");
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
