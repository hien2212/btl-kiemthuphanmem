package com.usermgmt.test.api;

import com.usermgmt.test.base.BaseTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserApiTest extends BaseTest {

    private String adminToken;
    private String userToken;
    private Long createdUserId;

    @BeforeClass
    public void setup() {
        setupRestAssured();
        adminToken = getAdminToken();
        userToken  = getUserToken();
    }

    // ===================== GET ALL USERS =====================

    @Test(description = "Admin lấy danh sách users thành công")
    public void testGetAllUsersAsAdmin() {
        test = extent.createTest("API - GetAllUsers: Admin thành công");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/api/admin/users")
        .then()
            .statusCode(200)
            .body("$", not(empty()))
            .body("[0].id", notNullValue())
            .body("[0].username", notNullValue());

        test.pass("Admin lấy được danh sách users");
    }

    @Test(description = "User thường bị 403 khi lấy danh sách users")
    public void testGetAllUsersAsUserForbidden() {
        test = extent.createTest("API - GetAllUsers: User thường bị 403");

        freshRequest()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/api/admin/users")
        .then()
            .statusCode(403);

        test.pass("Trả về 403 khi user thường gọi admin API");
    }

    @Test(description = "Không có token bị 401/403")
    public void testGetAllUsersNoToken() {
        test = extent.createTest("API - GetAllUsers: Không có token");

        // Dùng freshRequest() không kèm token, không kèm session
        freshRequest()
        .when()
            .get("/api/admin/users")
        .then()
            .statusCode(anyOf(equalTo(401), equalTo(403)));

        test.pass("Trả về 401/403 khi không có token");
    }

    // ===================== GET USER BY ID =====================

    @Test(description = "Admin lấy user theo ID thành công")
    public void testGetUserByIdSuccess() {
        test = extent.createTest("API - GetUserById: Thành công");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/api/admin/users/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("username", notNullValue());

        test.pass("Lấy thông tin user ID=1 thành công");
    }

    @Test(description = "Lấy user ID không tồn tại trả về 404")
    public void testGetUserByIdNotFound() {
        test = extent.createTest("API - GetUserById: Không tồn tại");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/api/admin/users/99999")
        .then()
            .statusCode(404);

        test.pass("Trả về 404 khi ID không tồn tại");
    }

    // ===================== UPDATE ROLE =====================

    @Test(description = "Tạo user mới để test", priority = 1)
    public void testCreateUserForTests() {
        test = extent.createTest("API - Setup: Tạo user để test");

        Map<String, String> body = new HashMap<>();
        body.put("username", randomUsername());
        body.put("email", randomEmail());
        body.put("password", "password123");
        body.put("confirmPassword", "password123");

        Response response = freshRequest()
            .body(body)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(200)
            .extract().response();

        createdUserId = response.jsonPath().getLong("id");
        test.pass("Tạo user thành công với ID: " + createdUserId);
    }

    @Test(description = "Đổi role thành ADMIN",
          dependsOnMethods = "testCreateUserForTests", priority = 2)
    public void testUpdateRoleToAdmin() {
        test = extent.createTest("API - UpdateRole: Đổi thành ROLE_ADMIN");

        Map<String, String> body = new HashMap<>();
        body.put("role", "ROLE_ADMIN");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
            .body(body)
        .when()
            .put("/api/admin/users/" + createdUserId + "/role")
        .then()
            .statusCode(200)
            .body("role", equalTo("ROLE_ADMIN"));

        test.pass("Đổi role thành ROLE_ADMIN thành công");
    }

    @Test(description = "Đổi role về ROLE_USER",
          dependsOnMethods = "testUpdateRoleToAdmin", priority = 3)
    public void testUpdateRoleToUser() {
        test = extent.createTest("API - UpdateRole: Đổi về ROLE_USER");

        Map<String, String> body = new HashMap<>();
        body.put("role", "ROLE_USER");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
            .body(body)
        .when()
            .put("/api/admin/users/" + createdUserId + "/role")
        .then()
            .statusCode(200)
            .body("role", equalTo("ROLE_USER"));

        test.pass("Đổi role về ROLE_USER thành công");
    }

    @Test(description = "Đổi role không hợp lệ trả về 400",
          dependsOnMethods = "testCreateUserForTests", priority = 2)
    public void testUpdateRoleInvalid() {
        test = extent.createTest("API - UpdateRole: Role không hợp lệ");

        Map<String, String> body = new HashMap<>();
        body.put("role", "ROLE_SUPERUSER");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
            .body(body)
        .when()
            .put("/api/admin/users/" + createdUserId + "/role")
        .then()
            .statusCode(400);

        test.pass("Trả về 400 khi role không hợp lệ");
    }

    @Test(description = "User thường đổi role bị 403",
          dependsOnMethods = "testCreateUserForTests", priority = 2)
    public void testUpdateRoleAsUserForbidden() {
        test = extent.createTest("API - UpdateRole: User thường bị 403");

        Map<String, String> body = new HashMap<>();
        body.put("role", "ROLE_ADMIN");

        freshRequest()
            .header("Authorization", "Bearer " + userToken)
            .body(body)
        .when()
            .put("/api/admin/users/" + createdUserId + "/role")
        .then()
            .statusCode(403);

        test.pass("Trả về 403 khi user thường đổi role");
    }

    // ===================== TOGGLE STATUS =====================

    @Test(description = "Disable user",
          dependsOnMethods = "testCreateUserForTests", priority = 4)
    public void testToggleUserDisable() {
        test = extent.createTest("API - Toggle: Disable user");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .put("/api/admin/users/" + createdUserId + "/toggle")
        .then()
            .statusCode(200)
            .body("enabled", equalTo(false));

        test.pass("Disable user thành công");
    }

    @Test(description = "Enable user",
          dependsOnMethods = "testToggleUserDisable", priority = 5)
    public void testToggleUserEnable() {
        test = extent.createTest("API - Toggle: Enable user");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .put("/api/admin/users/" + createdUserId + "/toggle")
        .then()
            .statusCode(200)
            .body("enabled", equalTo(true));

        test.pass("Enable user thành công");
    }

    // ===================== PROFILE =====================

    @Test(description = "User lấy profile bản thân")
    public void testGetMyProfile() {
        test = extent.createTest("API - GetProfile: User lấy profile");

        freshRequest()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/api/users/me")
        .then()
            .statusCode(200)
            .body("username", equalTo(USER_USERNAME))
            .body("role", equalTo("ROLE_USER"));

        test.pass("Lấy profile bản thân thành công");
    }

    @Test(description = "Admin lấy profile bản thân")
    public void testGetAdminProfile() {
        test = extent.createTest("API - GetProfile: Admin lấy profile");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/api/users/me")
        .then()
            .statusCode(200)
            .body("username", equalTo(ADMIN_USERNAME))
            .body("role", equalTo("ROLE_ADMIN"));

        test.pass("Admin lấy được profile");
    }

    @Test(description = "Không có token bị từ chối")
    public void testGetProfileNoToken() {
        test = extent.createTest("API - GetProfile: Không có token");

        freshRequest()
        .when()
            .get("/api/users/me")
        .then()
            .statusCode(anyOf(equalTo(401), equalTo(403)));

        test.pass("Trả về 401/403 khi không có token");
    }

    // ===================== DELETE =====================

    @Test(description = "Xoá user thành công",
          dependsOnMethods = "testToggleUserEnable", priority = 6)
    public void testDeleteUser() {
        test = extent.createTest("API - DeleteUser: Xoá thành công");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/api/admin/users/" + createdUserId)
        .then()
            .statusCode(200)
            .body("success", equalTo(true));

        test.pass("Xoá user thành công");
    }

    @Test(description = "Xoá user không tồn tại trả về 404",
          dependsOnMethods = "testDeleteUser", priority = 7)
    public void testDeleteUserNotFound() {
        test = extent.createTest("API - DeleteUser: Không tồn tại");

        freshRequest()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/api/admin/users/" + createdUserId)
        .then()
            .statusCode(404);

        test.pass("Trả về 404 khi xoá user không tồn tại");
    }

    @Test(description = "User thường xoá user bị 403")
    public void testDeleteUserForbidden() {
        test = extent.createTest("API - DeleteUser: User thường bị 403");

        freshRequest()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .delete("/api/admin/users/1")
        .then()
            .statusCode(403);

        test.pass("Trả về 403 khi user thường xoá");
    }
}
