package com.mlb.mlbportal.e2e;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import com.mlb.mlbportal.repositories.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import static com.mlb.mlbportal.utils.TestConstants.MATCHES_PATH;
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

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MatchControllerTest extends BaseE2ETest {
    private Team team1, team2;

    @Autowired
    private MatchRepository matchRepository;

    private Long matchId;

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

        this.matchId = this.matchRepository.findAll().getFirst().getId();
    }

    @Test
    @DisplayName("GET /api/v1/matches/{matchId} should return the match successfully")
    void testGetMatchById() {
        String url = MATCHES_PATH + this.matchId;

        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("id", is(this.matchId.intValue()));
    }

    @Test
    @DisplayName("GET /api/v1/matches/teamName/{teamName}?location=home should return home matches")
    void testGetHomeMatchesWithLocationParam() {
        String url = MATCHES_PATH + "teamName/" + TEST_TEAM1_NAME + "?location=home";

        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("content.size()", is(1))
                .body("content.awayTeam.name", hasItems(this.team2.getName()));
    }

    @Test
    @DisplayName("GET /api/v1/matches/teamName/{teamName}?location=away should return away matches")
    void testGetAwayMatchesWithLocationParam() {
        String url = MATCHES_PATH + "teamName/" + TEST_TEAM1_NAME + "?location=away";

        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("content.size()", is(1))
                .body("content.homeTeam.name", hasItems(this.team2.getName()));
    }

    @Test
    @DisplayName("GET /api/v1/matches/{teamName}?location=invalid should return 400 Bad Request")
    void testGetMatchesWithInvalidLocationParam() {
        String url = MATCHES_PATH + TEST_TEAM1_NAME + "?location=invalid";

        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/v1/matches/team/{teamName}?year=YYYY&month=MM should return all matches of the team in that month")
    void testGetMatchesOfTeamByMonth() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        String url = MATCHES_PATH + "team/" + TEST_TEAM1_NAME + "?year=" + year + "&month=" + month;

        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("homeTeam.name", hasItems(this.team1.getName(), this.team2.getName()))
                .body("awayTeam.name", hasItems(this.team1.getName(), this.team2.getName()));
    }
}