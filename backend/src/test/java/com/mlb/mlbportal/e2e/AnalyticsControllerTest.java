package com.mlb.mlbportal.e2e;

import com.mlb.mlbportal.models.analytics.VisibilityStats;
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

import static com.mlb.mlbportal.utils.TestConstants.STATS_PATH;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class StatsControllerTest extends BaseE2ETest {

    @Autowired
    private VisibilityStatsRepository visibilityStatsRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        VisibilityStats statsToday = new VisibilityStats();
        statsToday.setDate(LocalDate.now());
        statsToday.setVisualizations(10);
        statsToday.setNewUsers(5);
        statsToday.setDeletedUsers(1);
        this.visibilityStatsRepository.save(statsToday);
    }

    @Test
    @DisplayName("GET /api/v1/stats/visibility should return list of stats for a date range")
    void testGetVisibilityStats() {
        String dateFrom = LocalDate.now().minusDays(1).toString();
        String dateTo = LocalDate.now().plusDays(1).toString();
        String url = STATS_PATH + "/visibility";

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
    @DisplayName("POST /api/v1/stats/visibility/visualizations should increment visualization count")
    void testUpdateVisualizations() {
        String url = STATS_PATH + "/visibility/visualizations";

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
    @DisplayName("POST /api/v1/stats/visibility/registrations should increment new users count")
    void testUpdateNewUsers() {
        String url = STATS_PATH + "/visibility/registrations";

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
    @DisplayName("POST /api/v1/stats/visibility/losses should increment deleted users count")
    void testUpdateDeletedUsers() {
        String url = STATS_PATH + "/visibility/losses";

        given()
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"))
                .body("message", containsString("Deleted Users successfully updated"));
    }
}