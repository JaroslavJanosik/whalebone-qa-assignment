package cz.whalebone.pages;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public abstract class BasePage {

    protected final BrowserContext context;
    protected final Page page;

    protected BasePage(BrowserContext context, Page page) {
        this.context = context;
        this.page = page;
    }

    @Step("Navigate to {url}")
    protected void navigateTo(String url) {
        page.navigate(url);
    }
}
