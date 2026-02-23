package cz.whalebone.support;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import cz.whalebone.api.client.TeamsApiClient;
import cz.whalebone.config.Config;
import cz.whalebone.context.GUIContext;
import cz.whalebone.context.TestContext;
import cz.whalebone.reporting.BaseListeners;
import cz.whalebone.reporting.HasBrowserContext;
import cz.whalebone.reporting.HasPage;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.Objects;

/**
 * Base class for all UI tests.
 */
public abstract class BaseUiTest extends BaseListeners implements HasPage, HasBrowserContext {

    private static final ThreadLocal<TestContext> CTX = new ThreadLocal<>();
    private static final ThreadLocal<PlaywrightFixture> FIXTURE = new ThreadLocal<>();


    protected final TestContext ctx() {
        return Objects.requireNonNull(
                CTX.get(),
                "TestContext is not initialized. Did @BeforeMethod run?"
        );
    }

    protected final GUIContext gui() {
        return Objects.requireNonNull(
                ctx().getGui(),
                "GUIContext is not initialized for this test."
        );
    }

    protected final TeamsApiClient apiClient() {
        return new TeamsApiClient(Config.apiBaseUrl());
    }

    @Override
    public Page getPage() {
        TestContext tc = CTX.get();
        return (tc != null && tc.getGui() != null) ? tc.getGui().page : null;
    }

    @Override
    public BrowserContext getBrowserContext() {
        TestContext tc = CTX.get();
        return (tc != null && tc.getGui() != null) ? tc.getGui().context : null;
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        TestContext ctx = new TestContext(Config.apiBaseUrl(), Config.uiBaseUrl());
        CTX.set(ctx);

        PlaywrightFixture fixture = new PlaywrightFixture();
        FIXTURE.set(fixture);
        ctx.setGui(fixture.gui());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        PlaywrightFixture fixture = FIXTURE.get();
        try {
            if (fixture != null) {
                fixture.close(result);
            }
        } finally {
            FIXTURE.remove();
            CTX.remove();
        }
    }
}