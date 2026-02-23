package cz.whalebone.pages.playground;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import cz.whalebone.pages.BasePage;
import io.qameta.allure.Step;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgressBarPage extends BasePage {

    private static final String TITLE_SELECTOR = "h3:has-text('Progress Bar')";
    private static final String START_BUTTON = "button:has-text('Start')";
    private static final String STOP_BUTTON = "button:has-text('Stop')";
    private static final String PROGRESS_BAR = "#progressBar";

    private static final int POLL_INTERVAL_MS = 75;

    private final Locator title;
    private final Locator start;
    private final Locator stop;
    private final Locator progress;

    public ProgressBarPage(BrowserContext context, Page page) {
        super(context, page);
        this.title = page.locator(TITLE_SELECTOR);
        this.start = page.locator(START_BUTTON);
        this.stop = page.locator(STOP_BUTTON);
        this.progress = page.locator(PROGRESS_BAR);
    }

    @Step("Verify on Progress Bar page")
    public void verifyOnProgressBarPage() {
        title.first().waitFor();
        assertThat(page.url()).contains("progressbar");
    }

    @Step("Start progress bar")
    public void start() {
        start.click();
    }

    @Step("Stop progress bar")
    public void stop() {
        stop.click();
    }

    public int progressValue() {
        String v = progress.getAttribute("aria-valuenow");
        if (v == null) {
            throw new IllegalStateException("aria-valuenow is null on #progressBar");
        }
        return Integer.parseInt(v.trim());
    }

    @Step("Wait until progress bar reaches at least {value}%")
    public void waitUntilAtLeast(int value) {
        page.waitForFunction(
                "v => parseInt(document.querySelector('#progressBar').getAttribute('aria-valuenow')) >= v",
                value
        );
    }

    /**
     * Polls the progress value until it has not changed for {@code stableForMs} consecutive
     * milliseconds, or throws if {@code timeoutMs} elapses first.
     *
     * <p><b>Note on tolerance:</b> there is an inherent race between the JS timer that drives
     * the bar and the JVM reading {@code aria-valuenow}. The caller should account for a
     * ±1–2 point overshoot when asserting the final value.</p>
     */
    @Step("Wait until progress bar value stabilizes for {stableForMs}ms (timeout {timeoutMs}ms)")
    public void waitUntilValueStabilizes(long stableForMs, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Integer last = null;
        long stableSince = System.currentTimeMillis();

        while (System.currentTimeMillis() < deadline) {
            int now = progressValue();

            if (last == null || now != last) {
                stableSince = System.currentTimeMillis();
                last = now;
            } else if (System.currentTimeMillis() - stableSince >= stableForMs) {
                return;
            }

            page.waitForTimeout(POLL_INTERVAL_MS);
        }

        throw new AssertionError(
                "Progress bar value did not stabilize within " + timeoutMs + "ms. Last value: " + last);
    }
}
