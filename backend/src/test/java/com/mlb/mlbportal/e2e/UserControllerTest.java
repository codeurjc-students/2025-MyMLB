package com.mlb.mlbportal.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    
    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private final UserEntity user1 = new UserEntity("fonssi@gmail.com", "fonssi29");
    private final UserEntity user2 = new UserEntity("armin@gmail.com", "armiin13");

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.userRepository.deleteAll();
        this.userRepository.save(user1);
        this.userRepository.save(user2);
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("E2E Test: GET /api/users returns all users with correct fields")
    void getAllUsersRESTMethodTest() {
        given().contentType(ContentType.JSON).when().get("/api/users").then()
            .statusCode(200)
            .body("size()", is(2))
            .body("username", hasItems("fonssi29", "armiin13"))
            .body("email", hasItems("fonssi@gmail.com", "armin@gmail.com"));
    }
}