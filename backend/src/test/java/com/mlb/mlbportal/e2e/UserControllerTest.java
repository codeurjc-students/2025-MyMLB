package com.mlb.mlbportal.e2e;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.mlb.mlbportal.utils.TestConstants.*;

import com.mlb.mlbportal.dto.user.EditProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
    void testGetAllUsers() {
        given()
                 .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(200)
                .body("content.size()", is(2))
                .body("content.username", hasItems(USER1_USERNAME, USER2_USERNAME))
                .body("content.email", hasItems(USER1_EMAIL, USER2_EMAIL))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/users should edit the profile of the active user")
    void testEditProfile() {
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .body(new EditProfileRequest(NEW_EMAIL, NEW_PASSWORD, false))
                .when()
                .patch(USERS_PATH)
                .then()
                .statusCode(200)
                .body("email", equalTo(NEW_EMAIL));
    }

    @Test
    @DisplayName("POST /api/v1/users/picture should upload the new profile picture of the user")
    void testChangeProfilePicture() {
        String url = USERS_PATH + "/picture";
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .multiPart("file", "test.jpg", "fake-image".getBytes(), "image/jpg")
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("url", is("http://fake.cloudinary.com/test.jpg"))
                .body("publicId", is("fake123"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/picture should delete the current profile picture of the user")
    void testDeleteProfilePicture() {
        String url = USERS_PATH + "/picture";
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("Picture Deleted"));
    }

    @Test
    @DisplayName("GET /api/v1/users/profile should retrieve the profile information of the active user")
    void testGetUserProfile() {
        String url = USERS_PATH + "/profile";
        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("email", is(USER1_EMAIL));
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
                .body("message", is("Team Successfully Remove"));
    }
}