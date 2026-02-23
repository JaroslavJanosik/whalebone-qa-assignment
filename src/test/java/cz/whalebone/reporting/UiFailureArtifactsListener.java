package cz.whalebone.reporting;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;
import cz.whalebone.config.Config;
import io.qameta.allure.Allure;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Attaches UI failure artifacts to Allure at the *test method* phase (not in teardown),
 * so they appear at the test level and remain easy to spot.
 *
 * Why this listener:
 * TestNG's {@link org.testng.ITestListener#onTestFailure} may fire after @AfterMethod,
 * which makes Allure group attachments under the "Tear down" fixture.
 * {@link IInvokedMethodListener#afterInvocation} runs immediately after the test method,
 * before teardown, so attachments are displayed under the test itself.
 */
public class UiFailureArtifactsListener implements IInvokedMethodListener {

    private static final Logger LOG = Logger.getLogger(UiFailureArtifactsListener.class.getName());

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) return;
        if (result.getStatus() != ITestResult.FAILURE) return;

        Object instance = result.getInstance();
        BrowserContext ctx = (instance instanceof HasBrowserContext h) ? h.getBrowserContext() : null;
        Page page = (instance instanceof HasPage p) ? p.getPage() : null;

        // 1) Always try to attach quick context first (URL/DOM/screenshot)
        if (page != null) {
            attachText("URL on failure", safe(page::url));
            attachHtml("DOM snapshot (HTML)", safe(page::content));

            if (Config.uiScreenshotOnFailure()) {
                try {
                    byte[] png = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    Allure.addAttachment("Screenshot on failure", "image/png", new ByteArrayInputStream(png), ".png");
                } catch (Exception e) {
                    LOG.log(Level.FINE, "Failed to capture screenshot", e);
                }
            }
        }

        // 2) Trace (must be stopped while context is still alive)
        if (Config.uiTraceOnFailure() && ctx != null) {
            try {
                Path traceZip = uniqueArtifactPath(result, "trace.zip");
                Files.createDirectories(traceZip.getParent());
                ctx.tracing().stop(new Tracing.StopOptions().setPath(traceZip));
                attachFile("Playwright trace", "application/zip", traceZip, ".zip");
            } catch (Exception e) {
                LOG.log(Level.FINE, "Failed to stop/attach tracing", e);
            }
        }

        // 3) Video (finalizes only after page/context close)
        // To keep it visible at test-level, we finalize the video here (close the page) after taking screenshot/DOM.
        if (Config.uiVideoOnFailure() && page != null) {
            try {
                Video video = page.video();
                page.close();
                if (video != null) {
                    Path videoPath = video.path();
                    if (videoPath != null && Files.exists(videoPath)) {
                        attachFile("Video", "video/webm", videoPath, ".webm");
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.FINE, "Failed to finalize/attach video", e);
            }
        }
    }

    private static void attachText(String name, String value) {
        if (value == null || value.isBlank()) return;
        try {
            Allure.addAttachment(name, "text/plain",
                    new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)), ".txt");
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to attach text: " + name, e);
        }
    }

    private static void attachHtml(String name, String value) {
        if (value == null || value.isBlank()) return;
        try {
            Allure.addAttachment(name, "text/html",
                    new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)), ".html");
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to attach html: " + name, e);
        }
    }

    private static void attachFile(String name, String mime, Path path, String ext) {
        try {
            Allure.addAttachment(name, mime, Files.newInputStream(path), ext);
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to attach file: " + name + " from " + path, e);
        }
    }

    private static Path uniqueArtifactPath(ITestResult result, String fileName) {
        String safeTestName = sanitize(result.getTestClass().getName() + "." + result.getMethod().getMethodName()
                + "#" + result.getMethod().getCurrentInvocationCount());
        String ts = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
                .format(java.time.LocalDateTime.now());
        return Paths.get(Config.artifactsDir()).resolve(safeTestName).resolve(ts).resolve(fileName);
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static <T> T safe(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }
}
