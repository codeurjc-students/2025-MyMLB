package com.mlb.mlbportal.e2e;

import java.time.LocalDateTime;

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
import com.mlb.mlbportal.models.enums.MatchStatus;

import io.restassured.RestAssured;

import static com.mlb.mlbportal.utils.TestConstants.*;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MatchControllerTest extends BaseE2ETest {
    private Team team1, team2, team3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        this.team1 = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL,
                Division.EAST, TEST_TEAM1_LOGO);
        this.team2 = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.AL,
                Division.EAST, TEST_TEAM2_LOGO);
        this.team3 = saveTestTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.NL,
                Division.CENTRAL, TEST_TEAM3_LOGO);

        saveTestMatches(team1, team2, 0, 0, LocalDateTime.now().plusMinutes(5), MatchStatus.Scheduled);
        saveTestMatches(team2, team3, 4, 8, LocalDateTime.now(), MatchStatus.InProgress);
        saveTestMatches(team3, team1, 1, 0, LocalDateTime.now(), MatchStatus.Finished);
    }

    @Test
    @DisplayName("GET /api/matches/today should return all the matches scheduled for today")
    void testGetMatchesOfTheDay() {
        given()
        .baseUri(RestAssured.baseURI)
        .port(this.port)
        .contentType(ContentType.JSON)
        .when()
        .get(MATCHES_OF_DAY_PATH)
        .then()
        .statusCode(200)
        .body("size()", is(3))
        .body("homeTeam.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
        .body("awayTeam.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
        .body("status", hasItems("Scheduled", "InProgress", "Finished"));
    }
}