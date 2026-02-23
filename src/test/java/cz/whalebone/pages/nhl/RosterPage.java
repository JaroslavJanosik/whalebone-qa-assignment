package cz.whalebone.pages.nhl;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import cz.whalebone.pages.BasePage;
import io.qameta.allure.Step;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RosterPage extends BasePage {

    private static final String PAGE_TITLE_SELECTOR = "h1";
    private static final String ROSTER_ROWS_SELECTOR = "table tbody tr";
    private static final String ANCESTOR_TABLE_XPATH = "xpath=ancestor::table[1]";
    private static final String TABLE_HEADER_TH = "thead th";
    private static final String ROW_TD_SELECTOR = "td";
    private static final String ROSTER_PATH_SUFFIX = "roster";

    private final Locator title;
    private final Locator rows;

    public RosterPage(BrowserContext context, Page page) {
        super(context, page);
        this.title = page.locator(PAGE_TITLE_SELECTOR);
        this.rows = page.locator(ROSTER_ROWS_SELECTOR);
    }

    @Step("Open roster page for {baseUrl}")
    public void open(String baseUrl) {
        navigateTo(buildRosterUrl(baseUrl));
    }

    static String buildRosterUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null/blank");
        }
        URI base = URI.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
        return base.resolve(ROSTER_PATH_SUFFIX).toString();
    }

    @Step("Verify roster page is loaded")
    public void verifyRosterPageLoaded() {
        title.first().waitFor();
        rows.first().waitFor();
    }

    /**
     * Scrapes the last column of each roster table row, which conventionally holds
     * the player's birth place.
     *
     * @return non-empty list of birthplace strings
     * @throws IllegalStateException if no birthplace values could be scraped
     */
    @Step("Scrape player birthplaces from roster table")
    public List<String> scrapeBirthplaces() {
        verifyRosterPageLoaded();

        int rowCount = rows.count();
        List<String> out = new ArrayList<>();

        Locator table = rows.first().locator(ANCESTOR_TABLE_XPATH);
        int thCount = table.locator(TABLE_HEADER_TH).count();
        int lastColIndex = thCount > 0 ? thCount - 1 : -1;

        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);
            Locator tds = row.locator(ROW_TD_SELECTOR);
            int tdCount = tds.count();
            if (tdCount == 0) continue;

            int idx = lastColIndex >= 0 ? Math.min(lastColIndex, tdCount - 1) : tdCount - 1;
            String value = tds.nth(idx).innerText().trim();

            if (!value.isBlank()) {
                out.add(value);
            }
        }

        if (out.isEmpty()) {
            throw new IllegalStateException("Could not scrape any Birth Place values from roster table");
        }
        return out;
    }
}
