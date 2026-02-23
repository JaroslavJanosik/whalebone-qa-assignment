package cz.whalebone.tests.ui;

import cz.whalebone.support.BaseUiTest;
import cz.whalebone.util.Stopwatch;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Testing Playground")
@Feature("uitestingplayground.com")
public class UiTestingPlaygroundTests extends BaseUiTest {

    private static final int PROGRESS_STOP_TOLERANCE = 5;

    @Test
    @Story("Sample App")
    @Description("Covers successful login, logout, and rejected login with invalid credentials.")
    @Severity(SeverityLevel.CRITICAL)
    public void sampleApp_coverAllFunctionalities() {
        gui().playgroundHome().open(ctx().getUiBaseUrl());
        gui().playgroundHome().verifyOnHomePage();

        gui().playgroundHome().navigateToSampleApp();
        gui().sampleApp().verifyOnSampleAppPage();

        // Successful login
        gui().sampleApp().login("whaleboneUser", "pwd");
        gui().sampleApp().verifyWelcomeForUser("whaleboneUser");

        // Logout
        gui().sampleApp().logout();
        gui().sampleApp().verifyLoggedOut();

        // Invalid credentials
        gui().sampleApp().login("whaleboneUser", "wrong");
        gui().sampleApp().verifyInvalidLogin();
    }

    @Test
    @Story("Load Delay")
    @Description("Navigates to the Load Delay page and asserts the delayed button appears within 10 seconds.")
    @Severity(SeverityLevel.NORMAL)
    public void loadDelay_pageLoadsInReasonableTime() {
        gui().playgroundHome().open(ctx().getUiBaseUrl());
        gui().playgroundHome().verifyOnHomePage();

        Stopwatch sw = Stopwatch.start();
        gui().playgroundHome().navigateToLoadDelay();
        gui().loadDelay().waitForDelayedButton();
        long elapsedMs = sw.elapsedMs();

        assertThat(elapsedMs)
                .as("Delayed button should appear within 10 seconds")
                .isLessThan(10_000);
    }

    @Test
    @Story("Progress Bar")
    @Description("Starts the progress bar, waits until 75%, stops, then asserts the final value is in [75, 80].")
    @Severity(SeverityLevel.NORMAL)
    public void progressBar_followScenario_waitTo75_thenStop() {
        gui().playgroundHome().open(ctx().getUiBaseUrl());
        gui().playgroundHome().verifyOnHomePage();

        gui().playgroundHome().navigateToProgressBar();
        gui().progressBar().verifyOnProgressBarPage();

        int stopTarget = 75;

        gui().progressBar().start();
        gui().progressBar().waitUntilAtLeast(stopTarget);
        gui().progressBar().stop();
        gui().progressBar().waitUntilValueStabilizes(250, 5_000);

        int finalValue = gui().progressBar().progressValue();
        assertThat(finalValue)
                .as("Progress bar final value should be in [%d, %d] after stopping at %d%%",
                        stopTarget, stopTarget + PROGRESS_STOP_TOLERANCE, stopTarget)
                .isBetween(stopTarget, stopTarget + PROGRESS_STOP_TOLERANCE);
    }
}
