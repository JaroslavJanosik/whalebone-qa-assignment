package cz.whalebone.reporting;

import com.microsoft.playwright.BrowserContext;

public interface HasBrowserContext {
    BrowserContext getBrowserContext();
}
