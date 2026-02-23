package cz.whalebone.support;

import com.microsoft.playwright.*;
import cz.whalebone.config.Config;
import cz.whalebone.context.GUIContext;
import org.testng.ITestResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owns all Playwright lifecycle objects for a single test execution.
 *
 * <p>Design goals:
 * <ul>
 *   <li>Single place responsible for starting/stopping tracing and video capture</li>
 *   <li>Best-effort artifacts on failure without masking the test failure</li>
 *   <li>Predictable close order (page -> context -> browser -> playwright)</li>
 * </ul>
 */
public final class PlaywrightFixture implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(PlaywrightFixture.class.getName());

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;

    public PlaywrightFixture() {
        this.playwright = Playwright.create();
        this.browser = launchBrowser(playwright);

        Path artifactsBase = Paths.get(Config.artifactsDir()).toAbsolutePath();
        mkdirsQuietly(artifactsBase);

        Browser.NewContextOptions ctxOptions = new Browser.NewContextOptions()
                .setViewportSize(Config.uiViewportWidth(), Config.uiViewportHeight());

        if (Config.uiVideoOnFailure()) {
            Path videoDir = artifactsBase.resolve("videos");
            mkdirsQuietly(videoDir);
            ctxOptions.setRecordVideoDir(videoDir);
            ctxOptions.setRecordVideoSize(Config.uiViewportWidth(), Config.uiViewportHeight());
        }

        this.context = browser.newContext(ctxOptions);

        if (Config.uiTraceOnFailure()) {
            this.context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        this.page = context.newPage();
        this.page.setDefaultTimeout(Config.uiTimeoutMs());
        this.page.setDefaultNavigationTimeout(Config.uiTimeoutMs());
    }

    public GUIContext gui() {
        return new GUIContext(context, page);
    }

    public Page page() {
        return page;
    }

    public BrowserContext context() {
        return context;
    }

    public void close(ITestResult result) {
        boolean failed = result != null && result.getStatus() == ITestResult.FAILURE;

        if (!failed && Config.uiTraceOnFailure()) {
            try {
                context.tracing().stop();
            } catch (Exception e) {
                LOG.log(Level.FINE, "Failed to stop tracing for successful test", e);
            }
        }

        close();
    }

    @Override
    public void close() {
        closeQuietly(page);
        closeQuietly(context);
        closeQuietly(browser);
        closeQuietly(playwright);
    }

    private static Browser launchBrowser(Playwright pw) {
        String browserName = Config.uiBrowser().trim().toLowerCase(Locale.ROOT);

        BrowserType browserType = switch (browserName) {
            case "firefox" -> pw.firefox();
            case "webkit", "safari" -> pw.webkit();
            default -> pw.chromium();
        };

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(!Config.uiHeaded())
                .setSlowMo(Config.uiSlowMoMs());

        if ("msedge".equals(browserName) || "edge".equals(browserName)) {
            launchOptions.setChannel("msedge");
        } else if ("chrome".equals(browserName) || "googlechrome".equals(browserName)) {
            launchOptions.setChannel("chrome");
        }

        return browserType.launch(launchOptions);
    }

    private static void mkdirsQuietly(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to create directory: " + dir, e);
        }
    }

    private static void closeQuietly(AutoCloseable c) {
        try {
            if (c != null) c.close();
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to close resource", e);
        }
    }
}
