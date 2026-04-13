package com.mlb.mlbportal.e2e;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.mlb.mlbportal.utils.TestConstants.CACHE_PATH;
import static com.mlb.mlbportal.utils.TestConstants.SUCCESS;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CacheControllerTest extends BaseE2ETest {

    @Test
    @DisplayName("GET /api/v1/cache should return list of active caches")
    void testGetCaches() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(CACHE_PATH)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/cache/{name} should clear a specific cache")
    void testClearSingleCache() {
        String url = CACHE_PATH + "/get-standings";

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", containsString("Cache: get-standings Successfully Cleared"));
    }

    @Test
    @DisplayName("DELETE /api/v1/cache should clear all caches")
    void testClearAllCaches() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(CACHE_PATH)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("All Caches Successfully Cleared"));
    }
}