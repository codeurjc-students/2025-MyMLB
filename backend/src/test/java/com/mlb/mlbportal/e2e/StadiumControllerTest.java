package com.mlb.mlbportal.e2e;

import java.util.Collections;
import java.util.Map;

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
import static org.hamcrest.Matchers.*;

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
    @DisplayName("GET /api/v1/stadiums should return all stadiums with the correct data")
    void testGetAllStadiums() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get(ALL_STADIUMS_PATH)
                .then()
                .statusCode(200)
                .body("content.size()", is(3))
                .body("content.name", hasItems(STADIUM1_NAME, STADIUM2_NAME, STADIUM3_NAME))
                .body("content.openingDate", hasItems(STADIUM1_YEAR, STADIUM2_YEAR, STADIUM3_YEAR))
                .body("content.teamName", hasItems(this.team1.getName(), this.team2.getName(), this.team3.getName()))
                .body("page.size", is(10))
                .body("page.totalElements", is(3))
                .body("page.totalPages", is(1));
    }

    @Test
    @DisplayName("GET /api/v1/stadiums/available should return all available stadiums")
    void testGetAvailableStadiums() {
        saveTestStadiums(NEW_STADIUM, NEW_STADIUM_YEAR, null);
        String url = ALL_STADIUMS_PATH + "/available";
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("content.size()", is(1))
                .body("content.name", hasItem(NEW_STADIUM))
                .body("content.openingDate", hasItem(NEW_STADIUM_YEAR))
                .body("content.teamName", everyItem(nullValue()))
                .body("page.size", is(10))
                .body("page.totalElements", is(1))
                .body("page.totalPages", is(1));
        cleanDatabase();
    }

    @Test
    @DisplayName("GET /api/v1/stadiums/{name} should return the information of a stadium based on its name")
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

    private String picturesUrl(String stadiumName) {
        return STADIUM_PATH + stadiumName + "/pictures";
    }

    /**
     * Helper method that uploads a picture for stadium picture test cases.
     * <p>
     * This method is used to avoid duplication in E2E tests by encapsulating
     * the RestAssured call that posts a picture to the stadium endpoint and
     * validates the expected response. It ensures that the picture upload
     * returns a successful status code and the expected URL and publicId.
     * </p>
     *
     * @param fileName the name of the file to be uploaded (e.g. "test.png")
     * @param content the content of the file as a string, which will be converted to bytes
     */
    private void uploadPicture(String fileName, String content) {
        given()
                .multiPart("file", fileName, content.getBytes())
                .accept(ContentType.JSON)
                .when()
                .post(this.picturesUrl(STADIUM1_NAME))
                .then()
                .statusCode(200)
                .body("url", is("http://fake.cloudinary.com/test.jpg"))
                .body("publicId", is("fake123"));
    }

    @Test
    @DisplayName("GET /api/v1/stadiums/{stadiumName}/pictures should return all pictures of the stadium")
    void testGetStadiumPictures() {
        this.uploadPicture("test.png", "fake-image");

        given()
                .accept(ContentType.JSON)
                .when()
                .get(this.picturesUrl(STADIUM1_NAME))
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].url", is("http://fake.cloudinary.com/test.jpg"))
                .body("[0].publicId", is("fake123"));
    }

    @Test
    @DisplayName("POST /api/v1/stadiums/{stadiumName}/pictures should add the new picture to the pictureList of the stadium")
    void testAddPicture() {
        this.uploadPicture("test.png", "fake-image");
    }

    @Test
    @DisplayName("DELETE /api/v1/stadiums/{stadiumName}/pictures should remove picture from stadium")
    void testDeletePicture() {
        this.uploadPicture("test.png", "fake-image");
        this.uploadPicture("test2.png", "fake2-image");

        given()
                .queryParam("publicId", "fake123")
                .accept(ContentType.JSON)
                .when()
                .delete(this.picturesUrl(STADIUM1_NAME))
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /api/v1/stadiums should create a new stadium and return 201 with Location header")
    void testCreateStadium() {
        Map<String, Object> requestBody = Map.of(
                "name", NEW_STADIUM,
                "openingDate", NEW_STADIUM_YEAR
        );

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(ALL_STADIUMS_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(STADIUM_PATH +"New%20Stadium"))
                .body("name", is(NEW_STADIUM))
                .body("openingDate", is(NEW_STADIUM_YEAR));
    }
}