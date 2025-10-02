package com.mlb.mlbportal.e2e;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.mlb.mlbportal.utils.TestConstants.*;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest extends BaseE2ETest {

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        saveTestUser(USER1_EMAIL, USER1_USERNAME, USER1_PASSWORD);
        saveTestUser(USER2_EMAIL, USER2_USERNAME, USER2_PASSWORD);
    }

    @Test
    @DisplayName("E2E Test: GET /api/users returns all users with correct fields")
    void getAllUsersRESTMethodTest() {
        given().contentType(ContentType.JSON).when().get("/api/users").then()
            .statusCode(200)
            .body("size()", is(2))
            .body("username", hasItems(USER1_USERNAME, USER2_USERNAME))
            .body("email", hasItems(USER1_EMAIL, USER2_EMAIL));
    }
}