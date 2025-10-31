package com.mlb.mlbportal.e2e;

import static com.mlb.mlbportal.utils.TestConstants.*;

import static org.hamcrest.Matchers.hasItems;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PlayerControllerTest extends BaseE2ETest {
    
    private Team team1, team2;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();

        this.team1 = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES,
                TEST_TEAM1_CITY, TEST_TEAM1_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        this.team2 = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES,
                TEST_TEAM2_CITY, TEST_TEAM2_INFO, Collections.emptyList(), League.AL,
                Division.EAST);

        saveTestPositionPlayers(PLAYER1_NAME, team1, PLAYER1_AT_BATS, PLAYER1_WALKS, PLAYER1_HITS, PLAYER1_DOUBLES, PLAYER1_TRIPLES, PLAYER1_HOME_RUNS, PLAYER1_RBIS);
        saveTestPositionPlayers(PLAYER2_NAME, team1, PLAYER2_AT_BATS, PLAYER2_WALKS, PLAYER2_HITS, PLAYER2_DOUBLES, PLAYER2_TRIPLES, PLAYER2_HOME_RUNS, PLAYER2_RBIS);
        saveTestPitchers(PLAYER3_NAME, team2, PLAYER3_GAMES, PLAYER3_WINS, PLAYER3_LOSSES, PLAYER3_INNINGS, PLAYER3_SO, PLAYER3_WALKS, PLAYER3_HITS_ALLOWED, PLAYER3_RUNS_ALLOWED, PLAYER3_SAVES, PLAYER3_SAVES_OPORTUNITIES);
    }

    @Test
    @DisplayName("GET /api/players should return all players with the correct data")
    void testGetAllPlayers() {
        given()
            .accept(ContentType.JSON)
            .when()
            .get(ALL_PLAYERS_PATH)
            .then()
            .statusCode(200)
            .body("size()", is(3))
            .body("name", hasItems(PLAYER1_NAME, PLAYER2_NAME, PLAYER3_NAME))
            .body("teamName", hasItems(TEST_TEAM1_NAME, TEST_TEAM1_NAME, TEST_TEAM2_NAME));
    }

    @Test
    @DisplayName("GET /api/players/position-players should return all position players with the correct data")
    void testGetAllPositionPlayers() {
        given()
            .accept(ContentType.JSON)
            .when()
            .get(ALL_POSITION_PLAYERS_PATH)
            .then()
            .statusCode(200)
            .body("size()", is(2))
            .body("name", hasItems(PLAYER1_NAME, PLAYER2_NAME))
            .body("teamName", hasItems(TEST_TEAM1_NAME, TEST_TEAM1_NAME));
    }

    @Test
    @DisplayName("GET /api/players/pitchers should return all pitchers with the correct data")
    void testGetAllPitchers() {
        given()
            .accept(ContentType.JSON)
            .when()
            .get(ALL_PITCHERS_PATH)
            .then()
            .statusCode(200)
            .body("size()", is(1))
            .body("name", hasItems(PLAYER3_NAME))
            .body("teamName", hasItems(TEST_TEAM2_NAME));
    }

    @Test
    @DisplayName("GET /api/players/{name} should return the position player with their respective name")
    void testGetPositionPlayerByName() {
        String url = ALL_PLAYERS_PATH + "/" + PLAYER1_NAME;
        given()
            .accept(ContentType.JSON)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .body("name", is(PLAYER1_NAME))
            .body("teamName", is(TEST_TEAM1_NAME))
            .body("hits", is(PLAYER1_HITS));
    }

    @Test
    @DisplayName("GET /api/players/{name} should return the pitcher with their respective name")
    void testGetPitcherByName() {
        String url = ALL_PLAYERS_PATH + "/" + PLAYER3_NAME;
        given()
            .accept(ContentType.JSON)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .body("name", is(PLAYER3_NAME))
            .body("teamName", is(TEST_TEAM2_NAME))
            .body("wins", is(PLAYER3_WINS));
    }

    @Test
    @DisplayName("GET /api/players/{name} should return 404 if no player exists with the given name")
    void testGetNonExistentPlayer() {
        String url = ALL_PLAYERS_PATH + "/" + UNKNOWN_PLAYER;
        given()
            .accept(ContentType.JSON)
            .when()
            .get(url)
            .then()
            .statusCode(404)
            .body("status", is(FAILURE))
            .body("message", is("Player Not Found"))
            .body("error", is("Player Not Found"));
    }

    @Test
    @DisplayName("GET /api/players/position-players/{teamName} should return all position players of the given team")
    void testGetAllPositionsPlayerOfATeam() {
        String url = ALL_POSITION_PLAYERS_PATH + "/" + TEST_TEAM1_NAME;
        given()
            .accept(ContentType.JSON)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", is(2))
            .body("content.name", hasItems(PLAYER1_NAME, PLAYER2_NAME))
            .body("content.hits", hasItems(PLAYER1_HITS, PLAYER2_HITS))
            .body("page.size", is(10))
            .body("page.totalElements", is(2))
            .body("page.totalPages", is(1));
    }

    @Test
    @DisplayName("GET /api/players/pitchers/{teamName} should return all pitchers of the given team")
    void testGetAllPitchersOfATeam() {
        String url = ALL_PITCHERS_PATH + "/" + TEST_TEAM2_NAME;
        given()
            .accept(ContentType.JSON)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", is(1))
            .body("content.name", hasItems(PLAYER3_NAME))
            .body("content.wins", hasItems(PLAYER3_WINS))
            .body("page.size", is(10))
            .body("page.totalElements", is(1))
            .body("page.totalPages", is(1));
    }

    @Test
    @DisplayName("GET /api/players/pitchers/{teamName} should return 404 if no team exists with the given name")
    void testGetPlayersFromNonExistentTeam() {
        String url = ALL_PITCHERS_PATH + "/" + UNKNOWN_TEAM;
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