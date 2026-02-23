package cz.whalebone.reporting;

import com.microsoft.playwright.TimeoutError;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Converts a Playwright {@link TimeoutError} to an {@link AssertionError} before the
 * Allure listener processes the result.
 *
 * <p>Without this, Allure classifies timeouts as <em>BROKEN</em> (infrastructure error)
 * rather than <em>FAILED</em> (test assertion failure), which skews metrics and
 * makes dashboards harder to triage.</p>
 */
public class TimeoutAsFailureListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        if (result == null) return;
        Throwable t = result.getThrowable();
        if (t instanceof TimeoutError) {
            result.setThrowable(
                    new AssertionError("Playwright timeout: " + t.getMessage(), t));
        }
    }
}
