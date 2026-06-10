package com.usermgmt.test.listener;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.*;

/**
 * ExtentReportListener: tự động log PASS / FAIL / SKIP cho mọi test.
 * BaseTest.setupReport() gọi ExtentReportListener.setExtent(extent) để
 * đăng ký instance — không cần truy cập field của BaseTest.
 */
public class ExtentReportListener implements ITestListener {

    // Instance được inject từ BaseTest.@BeforeSuite
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    /** Gọi từ BaseTest.setupReport() sau khi khởi tạo ExtentReports */
    public static void setExtent(ExtentReports extentReports) {
        extent = extentReports;
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (extent == null) return;

        String desc = result.getMethod().getDescription();
        if (desc == null || desc.isBlank()) {
            desc = result.getMethod().getMethodName();
        }

        String className  = result.getTestClass().getName();
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        String category   = simpleName.contains("Api") ? "API" : "UI";

        ExtentTest extTest = extent
                .createTest("<b>[" + simpleName + "]</b> " + desc)
                .assignCategory(category)
                .assignCategory(simpleName);

        testThread.set(extTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest t = testThread.get();
        if (t != null) t.log(Status.PASS, "✅ PASSED (" + elapsed(result) + " ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest t = testThread.get();
        if (t != null) {
            t.log(Status.FAIL, "❌ FAILED (" + elapsed(result) + " ms)");
            t.fail(result.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest t = testThread.get();
        if (t != null) {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage() : "no reason";
            t.log(Status.SKIP, "⚠️ SKIPPED – " + reason);
        }
    }

    private long elapsed(ITestResult r) {
        return r.getEndMillis() - r.getStartMillis();
    }
}
