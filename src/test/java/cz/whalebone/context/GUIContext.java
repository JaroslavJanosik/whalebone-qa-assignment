package cz.whalebone.context;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import cz.whalebone.pages.nhl.RosterPage;
import cz.whalebone.pages.playground.LoadDelayPage;
import cz.whalebone.pages.playground.PlaygroundHomePage;
import cz.whalebone.pages.playground.ProgressBarPage;
import cz.whalebone.pages.playground.SampleAppPage;

/**
 * GUIContext holds Playwright browser objects and lazily provides Page Objects.
 *
 * <p>Page Objects are created on first access rather than eagerly in the constructor.
 * This means a test that only uses {@link SampleAppPage} pays no cost for constructing
 * {@link RosterPage}, and adding new pages does not require touching this class.</p>
 */
public class GUIContext {

    public final BrowserContext context;
    public final Page page;

    private PlaygroundHomePage playgroundHome;
    private SampleAppPage sampleApp;
    private LoadDelayPage loadDelay;
    private ProgressBarPage progressBar;
    private RosterPage rosterPage;

    public GUIContext(BrowserContext context, Page page) {
        this.context = context;
        this.page = page;
    }

    public PlaygroundHomePage playgroundHome() {
        if (playgroundHome == null) playgroundHome = new PlaygroundHomePage(context, page);
        return playgroundHome;
    }

    public SampleAppPage sampleApp() {
        if (sampleApp == null) sampleApp = new SampleAppPage(context, page);
        return sampleApp;
    }

    public LoadDelayPage loadDelay() {
        if (loadDelay == null) loadDelay = new LoadDelayPage(context, page);
        return loadDelay;
    }

    public ProgressBarPage progressBar() {
        if (progressBar == null) progressBar = new ProgressBarPage(context, page);
        return progressBar;
    }

    public RosterPage rosterPage() {
        if (rosterPage == null) rosterPage = new RosterPage(context, page);
        return rosterPage;
    }
}
