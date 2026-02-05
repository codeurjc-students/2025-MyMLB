package com.mlb.mlbportal.e2e;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import static com.mlb.mlbportal.utils.TestConstants.ALL_TEAMS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.STANDINGS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.SUCCESS;
import static com.mlb.mlbportal.utils.TestConstants.TEAM_INFO_PATH;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_WINS;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TeamControllerTest extends BaseE2ETest {

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, TEST_TEAM1_CITY, TEST_TEAM1_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, TEST_TEAM2_CITY, TEST_TEAM2_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        saveTestTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, TEST_TEAM3_CITY, TEST_TEAM3_INFO, Collections.emptyList(), League.NL,
                Division.CENTRAL);
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
                .body("AL.EAST[0].abbreviation", is(TEST_TEAM2_ABBREVIATION))
                .body("AL.EAST[1].abbreviation", is(TEST_TEAM1_ABBREVIATION))
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