package cz.whalebone.pages.playground;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import cz.whalebone.pages.BasePage;
import io.qameta.allure.Step;

public class LoadDelayPage extends BasePage {

    private static final String DELAY_BUTTON = "button:has-text('Button Appearing After Delay')";

    private final Locator delayedButton;

    public LoadDelayPage(BrowserContext context, Page page) {
        super(context, page);
        this.delayedButton = page.locator(DELAY_BUTTON);
    }

    @Step("Wait for delayed button to appear")
    public void waitForDelayedButton() {
        delayedButton.waitFor();
    }
}
