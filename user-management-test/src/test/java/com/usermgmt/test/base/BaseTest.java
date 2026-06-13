package com.usermgmt.test.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
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
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

    // ===== Thư mục lưu screenshot =====
    private static final String SCREENSHOT_DIR = "test-output/screenshots/";

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
        // Tạo thư mục screenshots nếu chưa có
        new File(SCREENSHOT_DIR).mkdirs();

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

    /**
     * Sau mỗi test: chụp screenshot nếu FAILED (UI test),
     * đồng thời kiểm tra driver còn sống không.
     */
    @AfterMethod
    public void afterMethod(ITestResult result) {
        // Chụp screenshot khi UI test thất bại
        if (result.getStatus() == ITestResult.FAILURE && driver != null) {
            captureScreenshot(result.getMethod().getMethodName());
        }

        // Recover driver nếu bị crash
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

    /**
     * Chụp screenshot và lưu vào test-output/screenshots/.
     * Tự động đính kèm vào ExtentReport nếu có test đang chạy.
     */
    protected String captureScreenshot(String testName) {
        if (driver == null) return null;
        try {
            String timestamp  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName   = testName + "_" + timestamp + ".png";
            Path   outputPath = Paths.get(SCREENSHOT_DIR + fileName);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(outputPath, screenshotBytes);

            // Đính kèm vào ExtentReport dưới dạng base64
            if (test != null) {
                String base64 = Base64.getEncoder().encodeToString(screenshotBytes);
                test.fail("📸 Screenshot khi thất bại:",
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
            }

            System.out.println("📸 Screenshot: " + outputPath.toAbsolutePath());
            return outputPath.toString();

        } catch (IOException e) {
            System.err.println("⚠️ Không chụp được screenshot: " + e.getMessage());
            return null;
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
