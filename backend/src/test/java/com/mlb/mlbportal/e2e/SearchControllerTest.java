package com.mlb.mlbportal.e2e;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SearchControllerTest extends BaseE2ETest {
    private Team team1, team2, team3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();

        this.team1 = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES,
                TEST_TEAM1_CITY, TEST_TEAM1_INFO, Collections.emptyList(), League.AL, Division.EAST);
        this.team2 = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES,
                TEST_TEAM2_CITY, TEST_TEAM2_INFO, Collections.emptyList(), League.AL, Division.EAST);
        this.team3 = saveTestTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES,
                TEST_TEAM3_CITY, TEST_TEAM3_INFO, Collections.emptyList(), League.NL, Division.CENTRAL);

        saveTestStadiums(STADIUM1_NAME, STADIUM1_YEAR, this.team1);
        saveTestStadiums(STADIUM2_NAME, STADIUM2_YEAR, this.team2);
        saveTestStadiums(STADIUM3_NAME, STADIUM3_YEAR, this.team3);

        saveTestPositionPlayers(PLAYER1_NAME, team1, PLAYER1_AT_BATS, PLAYER1_WALKS, PLAYER1_HITS, PLAYER1_DOUBLES,
                PLAYER1_TRIPLES, PLAYER1_HOME_RUNS, PLAYER1_RBIS);
        saveTestPositionPlayers(PLAYER2_NAME, team1, PLAYER2_AT_BATS, PLAYER2_WALKS, PLAYER2_HITS, PLAYER2_DOUBLES,
                PLAYER2_TRIPLES, PLAYER2_HOME_RUNS, PLAYER2_RBIS);
        saveTestPitchers(PLAYER3_NAME, team2, PLAYER3_GAMES, PLAYER3_WINS, PLAYER3_LOSSES, PLAYER3_INNINGS, PLAYER3_SO,
                PLAYER3_WALKS, PLAYER3_HITS_ALLOWED, PLAYER3_RUNS_ALLOWED, PLAYER3_SAVES, PLAYER3_SAVES_OPORTUNITIES);
    }

    @Test
    @DisplayName("Search stadiums by query")
    void searchStadiums() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("query", "St")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/searchs/stadium")
        .then()
            .statusCode(200)
            .body("content.size()", is(3))
            .body("content.name", hasItems(STADIUM1_NAME, STADIUM2_NAME, STADIUM3_NAME))
            .body("page.totalElements", is(3));
    }

    @Test
    @DisplayName("Search teams by query")
    void searchTeams() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("query", "te")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/searchs/team")
        .then()
            .statusCode(200)
            .body("content.size()", is(3))
            .body("content.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
            .body("page.totalElements", is(3));
    }

    @Test
    @DisplayName("Search position players by query")
    void searchPositionPlayers() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("query", "Pl")
            .queryParam("playerType", "position")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/searchs/player")
        .then()
            .statusCode(200)
            .body("content.size()", is(2))
            .body("content.name", hasItems(PLAYER1_NAME, PLAYER2_NAME))
            .body("page.totalElements", is(2));
    }

    @Test
    @DisplayName("Search pitchers by query")
    void searchPitchers() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("query", "Pl")
            .queryParam("playerType", "pitcher")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/searchs/player")
        .then()
            .statusCode(200)
            .body("content.size()", is(1))
            .body("content.name", hasItems(PLAYER3_NAME))
            .body("page.totalElements", is(1));
    }

    @Test
    @DisplayName("Invalid search type returns 400")
    void invalidSearchType() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("query", "test")
        .when()
            .get("/api/v1/searchs/invalidType")
        .then()
            .statusCode(400)
            .body(containsString("Invalid Search Type"));
    }
}