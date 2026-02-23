package cz.whalebone.tests.api;

import cz.whalebone.api.client.TeamsApiClient;
import cz.whalebone.api.model.Team;
import cz.whalebone.api.model.TeamsResponse;
import cz.whalebone.config.Config;
import io.qameta.allure.*;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("NHL Teams API")
@Feature("/api/teams")
public class TeamsApiTests {

    private TeamsApiClient client;
    private TeamsResponse teamsResponse;

    @BeforeClass
    public void beforeClass() {
        client = new TeamsApiClient(Config.apiBaseUrl());
    }

    @BeforeMethod
    public void fetchTeams() {
        teamsResponse = client.getTeams();
        assertThat(teamsResponse)
                .as("TeamsResponse must not be null")
                .isNotNull();
        assertThat(teamsResponse.teams())
                .as("Teams list must not be null")
                .isNotNull();
    }

    @Test
    @Story("Contract")
    @Description("Response must match the agreed JSON schema contract.")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyTeamsResponseMatchesSchema() {
        client.getTeamsRaw()
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/teams.schema.json"));
    }

    @Test
    @Story("Team count")
    @Description("The API must return exactly 32 NHL teams.")
    @Severity(SeverityLevel.BLOCKER)
    public void verifyTeamsCountIs32() {
        assertThat(teamsResponse.teams())
                .as("Expected exactly 32 teams in the response")
                .hasSize(32);
    }

    @Test
    @Story("Oldest team")
    @Description("The team with the smallest 'founded' year must be Montreal Canadiens (1909).")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyOldestTeamIsMontrealCanadiens() {
        Team oldest = teamsResponse.teams().stream()
                .min(Comparator.comparingInt(Team::founded))
                .orElseThrow(() -> new AssertionError("Team list is empty"));

        assertThat(oldest.name())
                .as("Oldest team name")
                .isEqualTo("Montreal Canadiens");
        assertThat(oldest.founded())
                .as("Oldest team founding year")
                .isEqualTo(1909);
    }

    @Test
    @Story("Multi-team cities")
    @Description("At least one city must have more than one team; New York must have exactly the Islanders and the Rangers.")
    @Severity(SeverityLevel.NORMAL)
    public void verifyThereIsACityWithMoreThanOneTeam() {
        assertThat(teamsResponse.teams())
                .as("API should return a non-empty list of teams")
                .isNotEmpty();

        Map<String, List<Team>> byCity = teamsResponse.teams().stream()
                .filter(t -> t.location() != null && !t.location().isBlank())
                .collect(Collectors.groupingBy(Team::location));

        boolean anyMultiTeamCity = byCity.values().stream().anyMatch(v -> v.size() > 1);
        assertThat(anyMultiTeamCity)
                .as("Expected at least one city to have more than 1 team")
                .isTrue();

        List<Team> nyTeams = byCity.get("New York");
        assertThat(nyTeams)
                .as("Expected 'New York' to have exactly 2 teams")
                .isNotNull()
                .hasSize(2);

        assertThat(nyTeams)
                .extracting(Team::name)
                .as("Expected exact team names for New York")
                .containsExactlyInAnyOrder("New York Islanders", "New York Rangers");
    }

    @Test
    @Story("Metropolitan Division")
    @Description("The Metropolitan division must contain exactly 8 named teams.")
    @Severity(SeverityLevel.NORMAL)
    public void verifyMetropolitanDivisionHas8Teams() {
        List<Team> metro = teamsResponse.teams().stream()
                .filter(t -> t.division() != null
                        && "Metropolitan".equalsIgnoreCase(t.division().name()))
                .toList();

        assertThat(metro)
                .as("Metropolitan division team count")
                .hasSize(8);

        assertThat(metro)
                .extracting(Team::name)
                .as("Metropolitan division team names")
                .containsExactlyInAnyOrder(
                        "Carolina Hurricanes",
                        "Columbus Blue Jackets",
                        "New Jersey Devils",
                        "New York Islanders",
                        "New York Rangers",
                        "Philadelphia Flyers",
                        "Pittsburgh Penguins",
                        "Washington Capitals"
                );
    }
}
