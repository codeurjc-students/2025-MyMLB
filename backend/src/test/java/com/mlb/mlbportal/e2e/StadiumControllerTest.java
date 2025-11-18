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
import static com.mlb.mlbportal.utils.TestConstants.*;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StadiumControllerTest extends BaseE2ETest {

    private Team team1, team2, team3;

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
        this.team3 = saveTestTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES,
                TEST_TEAM3_CITY, TEST_TEAM3_INFO, Collections.emptyList(), League.NL,
                Division.CENTRAL);

        saveTestStadiums(STADIUM1_NAME, STADIUM1_YEAR, this.team1);
        saveTestStadiums(STADIUM2_NAME, STADIUM2_YEAR, this.team2);
        saveTestStadiums(STADIUM3_NAME, STADIUM3_YEAR, this.team3);
    }

    @Test
    @DisplayName("GET /api/stadiums should return all stadiums with the correct data")
    void testGetAllStadiums() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get(ALL_STADIUMS_PATH)
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("name", hasItems(STADIUM1_NAME, STADIUM2_NAME, STADIUM3_NAME))
                .body("openingDate", hasItems(STADIUM1_YEAR, STADIUM2_YEAR, STADIUM3_YEAR))
                .body("teamName", hasItems(this.team1.getName(), this.team2.getName(), this.team3.getName()));
    }

    @Test
    @DisplayName("GET /api/stadiums/{name} should return the information of a stadium based on its name")
    void testGetStadiumByName() {
        String url = STADIUM_PATH + STADIUM1_NAME;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("name", is(STADIUM1_NAME))
                .body("openingDate", is(STADIUM1_YEAR))
                .body("teamName", is(this.team1.getName()));
    }

    @Test
    @DisplayName("GET /api/stadiums/{name} should return a 404 if the stadium does not exists")
    void testGetNonExistentStadium() {
        String url = STADIUM_PATH + UNKNOWN_STADIUM;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(404)
                .body("status", is(FAILURE))
                .body("message", is("Stadium Not Found"))
                .body("error", is("Stadium Not Found"));
    }

    @Test
    @DisplayName("POST /api/stadiums/{stadiumName}/pictures should add the new picture to the pictureList of the stadium")
    void testAddPicture() {
        String url = STADIUM_PATH + STADIUM1_NAME + "/pictures";
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
    @DisplayName("DELETE /api/stadiums/{stadiumName}/pictures should remove picture from stadium")
    void testDeletePicture() {
        String postUrl = STADIUM_PATH + STADIUM1_NAME + "/pictures";
        String deleteUrl = STADIUM_PATH + STADIUM1_NAME + "/pictures";

        given()
                .multiPart("file", "test.png", "fake-image".getBytes())
                .accept(ContentType.JSON)
                .when()
                .post(postUrl)
                .then()
                .statusCode(200)
                .body("publicId", is("fake123"));

        given()
                .queryParam("publicId", "fake123")
                .accept(ContentType.JSON)
                .when()
                .delete(deleteUrl)
                .then()
                .statusCode(204);
    }
}