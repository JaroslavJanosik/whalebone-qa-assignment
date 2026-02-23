package cz.whalebone.reporting;

import cz.whalebone.config.Config;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.PlaywrightException;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Simple configurable retry analyzer.
 *
 * <p>Retries are disabled when the configured count is 0.</p>
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    public enum Kind {UI, API}

    private final Kind kind;
    private int attempt = 0;

    public RetryAnalyzer(Kind kind) {
        this.kind = kind;
    }

    @Override
    public boolean retry(ITestResult result) {
        if (!isRetryable(result)) {
            return false;
        }
        int max = switch (kind) {
            case UI -> Math.max(0, Config.uiRetryCount());
            case API -> Math.max(0, Config.apiRetryCount());
        };
        if (attempt < max) {
            attempt++;
            return true;
        }
        return false;
    }

    /**
     * Conservative retry policy:
     * - Never retry assertion failures (they are deterministic)
     * - UI: retry common transient Playwright failures (timeouts, navigation/network flakes)
     * - API: retry only on IO-ish/transient exceptions (kept minimal here)
     */
    private boolean isRetryable(ITestResult result) {
        if (result == null) return false;
        Throwable t = result.getThrowable();
        if (t == null) return true;

        if (t instanceof AssertionError) return false;
        if (t instanceof IllegalArgumentException) return false;

        if (kind == Kind.UI) {
            if (t instanceof TimeoutError) return true;
            if (t instanceof PlaywrightException) {
                String msg = String.valueOf(t.getMessage());
                return msg.contains("net::")
                        || msg.contains("Navigation")
                        || msg.contains("Target closed")
                        || msg.contains("has been closed")
                        || msg.contains("Protocol error");
            }
        }

        return kind == Kind.API;
    }
}
