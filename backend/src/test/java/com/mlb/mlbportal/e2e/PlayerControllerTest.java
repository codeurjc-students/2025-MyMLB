package com.mlb.mlbportal.e2e;

import java.util.Collections;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import static com.mlb.mlbportal.utils.TestConstants.ALL_PITCHERS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.ALL_PLAYERS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.ALL_POSITION_PLAYERS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.FAILURE;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_AT_BATS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_DOUBLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_HITS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_HOME_RUNS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_RBIS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_TRIPLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_WALKS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_AT_BATS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_DOUBLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_HITS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_HOME_RUNS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_RBIS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_TRIPLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_WALKS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_GAMES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_HITS_ALLOWED;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_INNINGS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_RUNS_ALLOWED;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_SAVES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_SAVES_OPPORTUNITIES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_SO;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_WALKS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_WINS;
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
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_PLAYER;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PlayerControllerTest extends BaseE2ETest {

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

        saveTestPositionPlayers(PLAYER1_NAME, 24, team1, PLAYER1_AT_BATS, PLAYER1_WALKS, PLAYER1_HITS, PLAYER1_DOUBLES, PLAYER1_TRIPLES, PLAYER1_HOME_RUNS, PLAYER1_RBIS);
        saveTestPositionPlayers(PLAYER2_NAME, 32, team1, PLAYER2_AT_BATS, PLAYER2_WALKS, PLAYER2_HITS, PLAYER2_DOUBLES, PLAYER2_TRIPLES, PLAYER2_HOME_RUNS, PLAYER2_RBIS);
        saveTestPitchers(PLAYER3_NAME, 1, team2, PLAYER3_GAMES, PLAYER3_WINS, PLAYER3_LOSSES, PLAYER3_INNINGS, PLAYER3_SO, PLAYER3_WALKS, PLAYER3_HITS_ALLOWED, PLAYER3_RUNS_ALLOWED, PLAYER3_SAVES, PLAYER3_SAVES_OPPORTUNITIES);
    }

    @Test
    @DisplayName("GET /api/v1/players should return all players with the correct data")
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
    @DisplayName("GET /api/v1/players/position-players should return all position players with the correct data")
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
    @DisplayName("GET /api/v1/players/pitchers should return all pitchers with the correct data")
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
    @DisplayName("GET /api/v1/players/{name} should return the position player with their respective name")
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
    @DisplayName("GET /api/v1/players/{name} should return the pitcher with their respective name")
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
    @DisplayName("GET /api/v1/players/{name} should return 404 if no player exists with the given name")
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
                .body("error", is("Resource Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/players/position-players/{teamName} should return all position players of the given team")
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
    @DisplayName("GET /api/v1/players/pitchers/{teamName} should return all pitchers of the given team")
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
    @DisplayName("GET /api/v1/players/pitchers/{teamName} should return 404 if no team exists with the given name")
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
                .body("error", is("Resource Not Found"));
    }
}