package com.mlb.mlbportal.e2e;

import java.util.Collections;
import java.util.Map;

import static com.mlb.mlbportal.utils.TestConstants.*;
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

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PlayerControllerTest extends BaseE2ETest {

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();

        Team team1 = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES,
                TEST_TEAM1_CITY, TEST_TEAM1_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        Team team2 = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES,
                TEST_TEAM2_CITY, TEST_TEAM2_INFO, Collections.emptyList(), League.AL,
                Division.EAST);

        saveTestPositionPlayers(PLAYER1_NAME, PLAYER1_NUMBER, team1, PLAYER1_AT_BATS, PLAYER1_WALKS, PLAYER1_HITS, PLAYER1_DOUBLES, PLAYER1_TRIPLES, PLAYER1_HOME_RUNS, PLAYER1_RBIS);
        saveTestPositionPlayers(PLAYER2_NAME, PLAYER2_NUMBER, team1, PLAYER2_AT_BATS, PLAYER2_WALKS, PLAYER2_HITS, PLAYER2_DOUBLES, PLAYER2_TRIPLES, PLAYER2_HOME_RUNS, PLAYER2_RBIS);
        saveTestPitchers(PLAYER3_NAME, PLAYER3_NUMBER, team2, PLAYER3_GAMES, PLAYER3_WINS, PLAYER3_LOSSES, PLAYER3_INNINGS, PLAYER3_SO, PLAYER3_WALKS, PLAYER3_HITS_ALLOWED, PLAYER3_RUNS_ALLOWED, PLAYER3_SAVES, PLAYER3_SAVES_OPPORTUNITIES);
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
    @DisplayName("POST /api/v1/players should create a player")
    void testCreatePlayer() {
        Map<String, Object> requestBody = Map.of(
                "name", NEW_PLAYER_NAME,
                "playerNumber", NEW_PLAYER_NUMBER,
                "teamName", TEST_TEAM1_NAME,
                "position", "CF"
        );

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(ALL_POSITION_PLAYERS_PATH)
                .then()
                .statusCode(201)
                .body("name", is(NEW_PLAYER_NAME))
                .body("playerNumber", is(NEW_PLAYER_NUMBER));
    }

    @Test
    @DisplayName("POST /api/v1/players/{playerName}/pictures should update the player's picture")
    void testUploadPicture() {
        String url = ALL_PLAYERS_PATH + "/" + PLAYER1_NAME + "/pictures";

        given()
                .multiPart("file", "test.png", "fake-image".getBytes())
                .accept(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("url", is("http://fake.cloudinary.com/test.jpg"))
                .body("publicId", is("fake123"));
    }

    @Test
    @DisplayName("PATCH /api/v1/players/position-players/{playerName} should update the player")
    void testUpdatePlayer() {
        String url = ALL_POSITION_PLAYERS_PATH + "/" + PLAYER1_NAME;
        Map<String, Object> requestBody = Map.of("playerNumber", NEW_PLAYER_NUMBER);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch(url)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("Player successfully updated"));
    }

    @Test
    @DisplayName("DELETE /api/v1/players/{playerName} should delete the given player")
    void testDeletePlayer() {
        String url = ALL_PLAYERS_PATH + "/" + PLAYER1_NAME;

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .statusCode(200)
                .body("name", is(PLAYER1_NAME))
                .body("playerNumber", is(PLAYER1_NUMBER));
    }
}