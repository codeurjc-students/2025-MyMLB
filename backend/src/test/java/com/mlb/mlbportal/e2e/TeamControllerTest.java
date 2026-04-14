package com.mlb.mlbportal.e2e;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.TeamRepository;
import static com.mlb.mlbportal.utils.TestConstants.ALL_TEAMS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.STANDINGS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.SUCCESS;
import static com.mlb.mlbportal.utils.TestConstants.TEAM_INFO_PATH;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TeamControllerTest extends BaseE2ETest {
    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        Team team1 = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_CITY, TEST_TEAM1_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        Team team2 = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_CITY, TEST_TEAM2_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        saveTestTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_CITY, TEST_TEAM3_INFO, Collections.emptyList(), League.NL,
                Division.CENTRAL);

        team1.setHomeGamesPlayed(0);
        team1.setHomeGamesWins(0);
        team1.setRoadGamesPlayed(1);
        team1.setRoadGamesWins(1);

        team2.setRunsScored(3);
        team2.setRunsAllowed(5);
        team2.setHomeGamesPlayed(1);
        team2.setHomeGamesWins(0);
        team2.setRoadGamesPlayed(0);
        team2.setRoadGamesWins(0);

        this.teamRepository.saveAll(List.of(team1, team2));

        saveTestMatches(team1, team2, 5, 3, LocalDateTime.now().minusDays(4), MatchStatus.FINISHED);
        saveTestDailyStandings(team1, LocalDate.now().minusMonths(1), 1);
    }

    @Test
    @DisplayName("GET /api/v1/teams should return all teams with the correct data")
    void testGetAllTeams() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get(ALL_TEAMS_PATH)
                .then()
                .statusCode(200)
                .body("content.size()", is(3))
                .body("content.teamStats.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
                .body("page.size", is(10))
                .body("page.totalElements", is(3))
                .body("page.totalPages", is(1));
    }

    @Test
    @DisplayName("GET /ap/v1/teams/available should return all available teams")
    void testGetAvailableTeams() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get(ALL_TEAMS_PATH + "/available")
                .then()
                .statusCode(200)
                .body("content.size()", is(3))
                .body("content.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
                .body("page.size", is(10))
                .body("page.totalElements", is(3))
                .body("page.totalPages", is(1));
    }

    @Test
    @DisplayName("GET /api/v1/teams/standings return standings grouped by league and division, ordered by pct")
    void testGetStandings() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get(STANDINGS_PATH)
                .then()
                .statusCode(200)
                .body("AL.EAST.size()", is(2))
                .body("AL.EAST[0].abbreviation", is(TEST_TEAM1_ABBREVIATION))
                .body("AL.EAST[1].abbreviation", is(TEST_TEAM2_ABBREVIATION))
                .body("NL.CENTRAL.size()", is(1))
                .body("NL.CENTRAL[0].abbreviation", is(TEST_TEAM3_ABBREVIATION));
    }

    @Test
    @DisplayName("GET /api/v1/teams/{teamName} should return the information of a team based on its name")
    void testGetTeamInformation() {
        String url = TEAM_INFO_PATH + TEST_TEAM1_NAME;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("teamStats.name", is(TEST_TEAM1_NAME))
                .body("teamStats.abbreviation", is(TEST_TEAM1_ABBREVIATION))
                .body("city", is(TEST_TEAM1_CITY));
    }

    @Test
    @DisplayName("GET /api/v1/teams/{teamName}/analytics/wins-per-rival should return wins against certain rivals")
    void testGetWinsPerRival() {
        String url = TEAM_INFO_PATH + TEST_TEAM1_NAME + "/analytics/wins-per-rival";

        given()
                .accept(ContentType.JSON)
                .queryParam("rivalTeamNames", Set.of(TEST_TEAM2_NAME))
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].rivalTeamName", is(TEST_TEAM2_NAME));
    }

    @Test
    @DisplayName("GET /api/v1/teams/analytics/runs-per-rival should return run stats for certain teams")
    void testGetRunsStatsPerRival() {
        String url = TEAM_INFO_PATH + "analytics/runs-per-rival";

        given()
                .accept(ContentType.JSON)
                .queryParam("teams", TEST_TEAM1_NAME, TEST_TEAM2_NAME)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("teamName", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME));
    }

    @Test
    @DisplayName("GET /api/v1/teams/{teamName}/analytics/win-distribution should return home and away wins")
    void testGetWinDistribution() {
        String url = TEAM_INFO_PATH + TEST_TEAM1_NAME + "/analytics/win-distribution";

        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("teamName", is(TEST_TEAM1_NAME))
                .body("homeGames", is(0))
                .body("roadGames", is(1))
                .body("roadWins", is(1))
                .body("homeWinPct", is(0.0f))
                .body("roadWinPct", is(1.0f));
    }

    @Test
    @DisplayName("GET /api/v1/teams/analytics/historic-ranking should return the historic ranking of a certain team")
    void testGetHistoricRanking() {
        String url = TEAM_INFO_PATH + "analytics/historic-ranking";
        String dateFrom = LocalDate.now().minusMonths(1).toString();

        given()
                .accept(ContentType.JSON)
                .queryParam("teams", TEST_TEAM1_NAME)
                .queryParam("dateFrom", dateFrom)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("$", hasKey(TEST_TEAM1_NAME))
                .body("'" + TEST_TEAM1_NAME + "'.size()", is(1))
                .body("'" + TEST_TEAM1_NAME + "'[0].teamName", is(TEST_TEAM1_NAME))
                .body("'" + TEST_TEAM1_NAME + "'[0].rank", is(1));
    }

    @Test
    @DisplayName("POST /api/v1/teams/sync should trigger rankings update")
    void testRefreshStandings() {
        String url = TEAM_INFO_PATH + "sync";
        given()
                .accept(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(202)
                .body("status", is("SUCCESS"))
                .body("message", is("Standings successfully updated!"));
    }

    @Test
    @DisplayName("PATCH /api/v1/teams/{teamName} should update the team with the provided fields")
    void testUpdateTeam() {
        String url = TEAM_INFO_PATH + TEST_TEAM1_NAME;
        Map<String, Object> requestBody = Map.of(
                "city", "Updated City",
                "newChampionship", 2025,
                "newInfo", "Updated Info"
        );

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch(url)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("Team successfully updated"));
    }
}