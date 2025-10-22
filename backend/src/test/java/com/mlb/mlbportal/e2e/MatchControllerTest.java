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
import static com.mlb.mlbportal.utils.TestConstants.MATCHES_OF_DAY_PATH;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_WINS;

import static io.restassured.RestAssured.given;

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
        .when()
        .get(MATCHES_OF_DAY_PATH)
        .then()
        .statusCode(200)
        .body("content.size()", is(3))
        .body("content.homeTeam.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
        .body("content.awayTeam.name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME, TEST_TEAM3_NAME))
        .body("content.status", hasItems("Scheduled", "InProgress", "Finished"))
        .body("page.totalElements", is(3))
        .body("page.size", is(10));
    }
}