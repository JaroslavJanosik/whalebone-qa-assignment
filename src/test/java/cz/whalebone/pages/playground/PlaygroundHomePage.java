package cz.whalebone.pages.playground;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import cz.whalebone.pages.BasePage;
import io.qameta.allure.Step;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaygroundHomePage extends BasePage {

    private static final String HEADER_SELECTOR = "h1#title";
    private static final String SAMPLE_APP_LINK = "a:has-text('Sample App')";
    private static final String LOAD_DELAY_LINK = "a:has-text('Load Delay')";
    private static final String PROGRESS_BAR_LINK = "a:has-text('Progress Bar')";

    private final Locator header;
    private final Locator sampleAppLink;
    private final Locator loadDelayLink;
    private final Locator progressBarLink;

    public PlaygroundHomePage(BrowserContext context, Page page) {
        super(context, page);
        this.header = page.locator(HEADER_SELECTOR);
        this.sampleAppLink = page.locator(SAMPLE_APP_LINK);
        this.loadDelayLink = page.locator(LOAD_DELAY_LINK);
        this.progressBarLink = page.locator(PROGRESS_BAR_LINK);
    }

    /**
     * Opens the home page.
     *
     * @param baseUrl the base URL (e.g. from Config or injected in tests) — keeps this page
     *                object decoupled from the static Config singleton.
     */
    @Step("Open Playground home page")
    public void open(String baseUrl) {
        navigateTo(baseUrl);
    }

    @Step("Verify on Playground home page")
    public void verifyOnHomePage() {
        assertThat(page.url()).contains("uitestingplayground.com");
        header.first().waitFor();
    }

    @Step("Navigate to Sample App")
    public void navigateToSampleApp() {
        sampleAppLink.click();
    }

    @Step("Navigate to Load Delay")
    public void navigateToLoadDelay() {
        loadDelayLink.click();
    }

    @Step("Navigate to Progress Bar")
    public void navigateToProgressBar() {
        progressBarLink.click();
    }
}
