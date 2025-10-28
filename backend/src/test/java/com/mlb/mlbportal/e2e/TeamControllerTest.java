package com.mlb.mlbportal.e2e;

import java.util.Collections;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import static com.mlb.mlbportal.utils.TestConstants.*;

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
    @DisplayName("GET /api/teams should return all teams with the correct data")
    void testGetAllTeams() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get(ALL_TEAMS_PATH)
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("teamDTO.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
                .body("teamDTO.abbreviation",
                        hasItems(TEST_TEAM1_ABBREVIATION, TEST_TEAM2_ABBREVIATION, TEST_TEAM3_ABBREVIATION))
                .body("teamDTO.wins", hasItems(TEST_TEAM1_WINS, TEST_TEAM2_WINS, TEST_TEAM3_WINS))
                .body("teamDTO.pct", hasItems(0.469f, 0.563f, 0.077f))
                .body("teamDTO.gamesBehind", hasItems(0.0f, 14.0f, 0.0f));
    }

    @Test
    @DisplayName("GET /api/teams/standings return standings grouped by league and division, ordered by pct")
    void testGetStandingsEndpoint() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get(STANDINGS_PATH)
                .then()
                .statusCode(200)
                .body("AL.EAST.size()", is(2))
                .body("AL.EAST[0].abbreviation", is(TEST_TEAM2_ABBREVIATION))
                .body("AL.EAST[1].abbreviation", is(TEST_TEAM1_ABBREVIATION))
                .body("AL.EAST[0].gamesBehind", is(0.0f))
                .body("AL.EAST[1].gamesBehind", is(14.0f))
                .body("NL.CENTRAL.size()", is(1))
                .body("NL.CENTRAL[0].abbreviation", is(TEST_TEAM3_ABBREVIATION))
                .body("NL.CENTRAL[0].gamesBehind", is(0.0f));
    }

    @Test
    @DisplayName("GET /api/teams/{teamName} should return the information of a team based on its name")
    void testGetTeamInformation() {
        String url = TEAM_INFO_PATH + TEST_TEAM1_NAME;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("teamDTO.name", is(TEST_TEAM1_NAME))
                .body("teamDTO.abbreviation", is(TEST_TEAM1_ABBREVIATION))
                .body("city", is(TEST_TEAM1_CITY));
    }

    @Test
    @DisplayName("GET /api/teams/{teamName} should return a 404 if the team does not exists")
    void testGetNonExistentTeamInformation() {
        String url = TEAM_INFO_PATH + UNKNOWN_TEAM;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(404)
                .body("status", is(FAILURE))
                .body("message", is("Team Not Found"))
                .body("error", is("Team Not Found"));
    }
}