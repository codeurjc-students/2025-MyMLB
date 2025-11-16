package com.mlb.mlbportal.e2e;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MatchControllerTest extends BaseE2ETest {
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

        saveTestMatches(this.team1, this.team2, 2, 3, LocalDateTime.now(), MatchStatus.FINISHED);
        saveTestMatches(this.team2, this.team1, 0, 10, LocalDateTime.now().minusHours(1), MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("GET /api/matches/home/{teamName} should return all home matches of the given team")
    void testGetHomeMatchesOfATeam() {
        String url = MATCHES_PATH + "home/" + TEST_TEAM1_NAME;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("awayTeam.name", hasItems(this.team2.getName()));
    }

    @Test
    @DisplayName("GET /api/matches/away/{teamName} should return all home matches of the given team")
    void testGetAwayMatchesOfATeam() {
        String url = MATCHES_PATH + "away/" + TEST_TEAM1_NAME;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("homeTeam.name", hasItems(this.team2.getName()));
    }
}