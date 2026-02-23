package cz.whalebone.pages.playground;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import cz.whalebone.pages.BasePage;
import io.qameta.allure.Step;

import static org.assertj.core.api.Assertions.assertThat;

public class SampleAppPage extends BasePage {

    private static final String TITLE_SELECTOR = "h3:has-text('Sample App')";
    private static final String USERNAME_INPUT = "input[name='UserName']";
    private static final String PASSWORD_INPUT = "input[name='Password']";
    private static final String LOGIN_BUTTON = "button#login";
    private static final String LOGOUT_BUTTON = "button:has-text('Log Out')";
    private static final String LOGIN_BUTTON_TEXT = "button:has-text('Log In')";
    private static final String STATUS_LABEL = "#loginstatus";

    private final Locator title;
    private final Locator username;
    private final Locator password;
    private final Locator loginButton;
    private final Locator logoutButton;
    private final Locator loginButtonText;
    private final Locator status;

    public SampleAppPage(BrowserContext context, Page page) {
        super(context, page);
        this.title = page.locator(TITLE_SELECTOR);
        this.username = page.locator(USERNAME_INPUT);
        this.password = page.locator(PASSWORD_INPUT);
        this.loginButton = page.locator(LOGIN_BUTTON);
        this.logoutButton = page.locator(LOGOUT_BUTTON);
        this.loginButtonText = page.locator(LOGIN_BUTTON_TEXT);
        this.status = page.locator(STATUS_LABEL);
    }

    @Step("Verify on Sample App page")
    public void verifyOnSampleAppPage() {
        title.first().waitFor();
        assertThat(page.url()).contains("sampleapp");
    }

    @Step("Login as '{user}'")
    public void login(String user, String pwd) {
        username.fill(user);
        password.fill(pwd);
        loginButton.click();
    }

    @Step("Verify welcome message for '{user}'")
    public void verifyWelcomeForUser(String user) {
        status.waitFor();
        assertThat(status.innerText())
                .as("Status label should contain welcome message for user '%s'", user)
                .contains("Welcome")
                .contains(user);
        logoutButton.waitFor();
    }

    @Step("Verify invalid login message is shown")
    public void verifyInvalidLogin() {
        status.waitFor();
        assertThat(status.innerText())
                .as("Status label should show invalid credentials message")
                .containsIgnoringCase("Invalid username/password");
    }

    @Step("Logout")
    public void logout() {
        logoutButton.click();
    }

    @Step("Verify logged out state")
    public void verifyLoggedOut() {
        status.waitFor();
        assertThat(status.innerText())
                .as("Status label should confirm logout")
                .containsIgnoringCase("logged out");
        loginButtonText.waitFor();
    }
}
