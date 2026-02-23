package cz.whalebone.tests.ui;

import cz.whalebone.api.model.Team;
import cz.whalebone.api.model.TeamsResponse;
import cz.whalebone.support.BaseUiTest;
import cz.whalebone.util.CountryUtil;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("NHL Teams")
@Feature("Oldest team roster scrape")
public class OldestNHLTeamRosterScrapeTests extends BaseUiTest {

    @Test
    @Story("Birthplace distribution")
    @Description("""
            Uses the API to determine the oldest NHL team, then opens its roster page
            in a real browser and scrapes player birthplaces.
            Asserts that there are more Canadian-born players than US-born players.
            """)
    @Severity(SeverityLevel.NORMAL)
    public void scrapeOldestTeamRoster_verifyMoreCanadiansThanUSA() {
        TeamsResponse res = apiClient().getTeams();
        Team oldest = res.teams().stream()
                .min(Comparator.comparingInt(Team::founded))
                .orElseThrow(() -> new AssertionError("Team list from API was empty"));

        gui().rosterPage().open(oldest.officialSiteUrl());
        List<String> birthplaces = gui().rosterPage().scrapeBirthplaces();

        long canCount = birthplaces.stream()
                .filter(bp -> CountryUtil.endsWithCountryCode(bp, "CAN"))
                .count();

        long usaCount = birthplaces.stream()
                .filter(bp -> CountryUtil.endsWithCountryCode(bp, "USA"))
                .count();

        assertThat(canCount)
                .as("Canadian-born players (%d) should outnumber US-born players (%d) on the %s roster",
                        canCount, usaCount, oldest.name())
                .isGreaterThan(usaCount);
    }
}
