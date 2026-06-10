package com.usermgmt.test.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.usermgmt.test.listener.ExtentReportListener;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BaseTest {

    // ===== Config =====
    protected static final String BASE_URL     = "http://localhost:8080";
    protected static final String API_BASE_URL = "http://localhost:8080";

    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "admin123";
    protected static final String USER_USERNAME  = "user1";
    protected static final String USER_PASSWORD  = "user123";

    // ===== Selenium =====
    protected WebDriver driver;
    protected WebDriverWait wait;

    // ===== ExtentReports =====
    protected static ExtentReports extent;
    protected ExtentTest test;

    // ===== RestAssured config =====
    protected static final RestAssuredConfig NO_REDIRECT_CONFIG = RestAssuredConfig.config()
            .redirect(RedirectConfig.redirectConfig().followRedirects(false));

    @BeforeSuite
    public void setupReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/TestReport.html");
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("User Management – Test Report");
        spark.config().setReportName("Automation Test Results");
        spark.config().setTimeStampFormat("dd/MM/yyyy HH:mm:ss");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Project",     "User Management System");
        extent.setSystemInfo("Environment", "Local – http://localhost:8080");
        extent.setSystemInfo("Framework",   "TestNG + Selenium + RestAssured");
        extent.setSystemInfo("Java",        System.getProperty("java.version"));

        // Đăng ký instance vào Listener để tự động log PASS/FAIL/SKIP
        ExtentReportListener.setExtent(extent);

        System.out.println("📊 ExtentReport sẽ lưu tại: test-output/TestReport.html");
    }

    @AfterSuite
    public void tearDownReport() {
        if (extent != null) {
            extent.flush();
            System.out.println("✅ ExtentReport đã được lưu.");
        }
    }

    // ===== Selenium setup =====
    protected void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--blink-settings=imagesEnabled=false");
        }

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments("--window-size=1366,768");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    protected void tearDownDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @AfterMethod
    public void recoverDriverIfCrashed(ITestResult result) {
        if (driver != null) {
            try {
                driver.getCurrentUrl();
            } catch (Exception e) {
                try { driver.quit(); } catch (Exception ignored) {}
                driver = null;
                wait = null;
                try { setupDriver(); } catch (Exception ignored) {}
            }
        }
    }

    // ===== RestAssured =====
    protected void setupRestAssured() {
        RestAssured.baseURI = API_BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected RequestSpecification freshRequest() {
        return RestAssured.given()
                .config(NO_REDIRECT_CONFIG)
                .contentType(ContentType.JSON);
    }

    // ===== JWT helpers =====
    protected String getAdminToken() { return getToken(ADMIN_USERNAME, ADMIN_PASSWORD); }
    protected String getUserToken()  { return getToken(USER_USERNAME,  USER_PASSWORD);  }

    protected String getToken(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        Response response = RestAssured
                .given()
                .config(NO_REDIRECT_CONFIG)
                .baseUri(API_BASE_URL)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().response();

        return response.jsonPath().getString("token");
    }

    protected String randomUsername() { return "testuser_" + System.currentTimeMillis(); }
    protected String randomEmail()    { return "test_" + System.currentTimeMillis() + "@test.com"; }
}
