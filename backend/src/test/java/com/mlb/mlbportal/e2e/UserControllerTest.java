package com.mlb.mlbportal.e2e;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import static com.mlb.mlbportal.utils.TestConstants.FAV_TEAMS_PATH;
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
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_WINS;
import static com.mlb.mlbportal.utils.TestConstants.USER1_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER1_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.USER2_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER2_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.USER2_USERNAME;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest extends BaseE2ETest {
    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        UserEntity user1 = saveTestUser(USER1_EMAIL, USER1_USERNAME, USER1_PASSWORD);
        saveTestUser(USER2_EMAIL, USER2_USERNAME, USER2_PASSWORD);

        Team team1 = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, TEST_TEAM1_CITY, TEST_TEAM1_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        Team team2 = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, TEST_TEAM2_CITY, TEST_TEAM2_INFO, Collections.emptyList(), League.AL,
                Division.EAST);
        Team team3 = saveTestTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, TEST_TEAM3_CITY, TEST_TEAM3_INFO, Collections.emptyList(), League.NL,
                Division.CENTRAL);

        user1.setFavTeams(new HashSet<>(List.of(team1, team2)));
        this.userRepository.save(user1);
    }

    @Test
    @DisplayName("GET /api/v1/users returns all users with correct fields")
    void getAllUsersRESTMethodTest() {
        given().contentType(ContentType.JSON).when().get("/api/v1/users").then()
            .statusCode(200)
            .body("size()", is(2))
            .body("username", hasItems(USER1_USERNAME, USER2_USERNAME))
            .body("email", hasItems(USER1_EMAIL, USER2_EMAIL));
    }

    @Test
    @DisplayName("GET /api/v1/users/favorites/teams should return all favorite teams of te active user")
    void testGetFavTeams() {
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .get(FAV_TEAMS_PATH)
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("name", hasItems(TEST_TEAM1_NAME, TEST_TEAM2_NAME));
    }

    @Test
    @DisplayName("POST /api/v1/users/favorites/teams/{teamName} should add a team to favorites")
    void testAddFavTeam() {
        String url = FAV_TEAMS_PATH + "/" + TEST_TEAM3_NAME;
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"))
                .body("message", is("Team Succesfullly Added"));
    }

    @Test
    @DisplayName("POST /api/v1/users/favorites/teams/{teamName} should fail if team is already favorite")
    void testAddExistantFavTeam() {
        String url = FAV_TEAMS_PATH + "/" + TEST_TEAM1_NAME;
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(409)
                .body("message", is("Team Already Exists"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/favorites/teams/{teamName} should remove a team from favorites")
    void testRemoveFavTeam() {
        String url = FAV_TEAMS_PATH + "/" + TEST_TEAM2_NAME;
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"))
                .body("message", is("Team Succesfullly Remove"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/favorites/teams/{teamName} should fail if team is not in favorites")
    void testRemoveNonFavTeam() {
        String url = FAV_TEAMS_PATH + "/" + TEST_TEAM3_NAME;
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .statusCode(404)
                .body("message", is("Team Not Found"));
    }
}