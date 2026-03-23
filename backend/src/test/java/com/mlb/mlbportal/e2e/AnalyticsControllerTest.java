package com.mlb.mlbportal.e2e;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.analytics.APIPerformance;
import com.mlb.mlbportal.models.analytics.VisibilityStats;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.repositories.analytics.APIPerformanceRepository;
import com.mlb.mlbportal.repositories.analytics.VisibilityStatsRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mlb.mlbportal.utils.TestConstants.ANALYTICS_PATH;
import static com.mlb.mlbportal.utils.TestConstants.AVG_TIME1;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TOTAL_ERRORS1;
import static com.mlb.mlbportal.utils.TestConstants.TOTAL_REQUESTS1;
import static com.mlb.mlbportal.utils.TestConstants.TOTAL_SUCCESSES1;
import static com.mlb.mlbportal.utils.TestConstants.USER1_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER1_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AnalyticsControllerTest extends BaseE2ETest {

    @Autowired
    private VisibilityStatsRepository visibilityStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private APIPerformanceRepository apiPerformanceRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        VisibilityStats statsToday = new VisibilityStats();
        statsToday.setDate(LocalDate.now());
        statsToday.setVisualizations(10);
        statsToday.setNewUsers(5);
        statsToday.setDeletedUsers(1);
        this.visibilityStatsRepository.save(statsToday);

        UserEntity user = saveTestUser(USER1_EMAIL, USER1_USERNAME, USER1_PASSWORD);
        Team team = saveTestTeam("Team1", TEST_TEAM1_ABBREVIATION, TEST_TEAM1_CITY, TEST_TEAM1_INFO, List.of(2009), League.AL, Division.EAST);

        Set<Team> favTeams = new HashSet<>();
        favTeams.add(team);
        user.setFavTeams(favTeams);
        this.userRepository.save(user);

        APIPerformance apiPerformance = new APIPerformance(LocalDateTime.now(), TOTAL_REQUESTS1, TOTAL_ERRORS1, TOTAL_SUCCESSES1, AVG_TIME1, Collections.emptyList());
        this.apiPerformanceRepository.save(apiPerformance);
    }

    @Test
    @DisplayName("GET /api/v1/analytics/visibility should return list of stats for a date range")
    void testGetVisibilityStats() {
        String dateFrom = LocalDate.now().minusDays(1).toString();
        String dateTo = LocalDate.now().plusDays(1).toString();
        String url = ANALYTICS_PATH + "/visibility";

        given()
                .queryParam("dateFrom", dateFrom)
                .queryParam("dateTo", dateTo)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].visualizations", is(10))
                .body("[0].newUsers", is(5));
    }

    @Test
    @DisplayName("POST /api/v1/analytics/visibility/visualizations should increment visualization count")
    void testUpdateVisualizations() {
        String url = ANALYTICS_PATH + "/visibility/visualizations";

        given()
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"))
                .body("message", containsString("Visualizations successfully updated"));
    }

    @Test
    @DisplayName("POST /api/v1/analytics/visibility/registrations should increment new users count")
    void testUpdateNewUsers() {
        String url = ANALYTICS_PATH + "/visibility/registrations";

        given()
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"))
                .body("message", containsString("New Users successfully updated"));
    }

    @Test
    @DisplayName("POST /api/v1/analytics/visibility/losses should increment deleted users count")
    void testUpdateDeletedUsers() {
        String url = ANALYTICS_PATH + "/visibility/losses";

        given()
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"))
                .body("message", containsString("Deleted Users successfully updated"));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/fav-teams should return the favorite teams with their number of fans")
    void testGetFavTeamsAnalytics() {
        String url = ANALYTICS_PATH + "/fav-teams";

        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("Team1 ", is(1));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/api-performance should return the api analytics in the given date range")
    void testGetAPIPerformanceAnalytics() {
        String url = ANALYTICS_PATH + "/api-performance";

        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].totalRequests", is(100));
    }
}