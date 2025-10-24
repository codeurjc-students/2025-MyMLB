package com.mlb.mlbportal.e2e;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static com.mlb.mlbportal.utils.TestConstants.*;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TeamControllerTest extends BaseE2ETest {

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL,
                Division.EAST, TEST_TEAM1_LOGO);
        saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.AL,
                Division.EAST, TEST_TEAM2_LOGO);
        saveTestTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.NL,
                Division.CENTRAL, TEST_TEAM3_LOGO);
    }

    @Test
    @DisplayName("GET /api/teams should return all teams with the correct data")
    void testGetAllTeams() {
        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .when()
                .get(ALL_TEAMS_PATH)
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
                .body("abbreviation",
                        hasItems(TEST_TEAM1_ABBREVIATION, TEST_TEAM2_ABBREVIATION, TEST_TEAM3_ABBREVIATION))
                .body("wins", hasItems(TEST_TEAM1_WINS, TEST_TEAM2_WINS, TEST_TEAM3_WINS))
                .body("pct", hasItems(0.469f, 0.563f, 0.077f))
                .body("gamesBehind", hasItems(0.0f, 14.0f, 0.0f));
    }

    @Test
    @DisplayName("GET /api/teams/standings return standings grouped by league and division, ordered by pct")
    void testGetStandingsEndpoint() {
        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
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
}