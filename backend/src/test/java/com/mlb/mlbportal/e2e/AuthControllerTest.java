package com.mlb.mlbportal.e2e;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.mlb.mlbportal.utils.TestConstants.LOGIN_PATH;
import static com.mlb.mlbportal.utils.TestConstants.REGISTER_PATH;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest extends BaseE2ETest {

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        saveTestUser(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);
    }

    @Test
    @DisplayName("POST /api/auth/login should authenticate user and return tokens in cookies")
    void testLoginUserRequest() {
        String requestBody = """
                {
                    "username" : "%s",
                    "password" : "%s"
                }
                """.formatted(TEST_USER_USERNAME, TEST_USER_PASSWORD);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("message", equalTo("Auth successful. Tokens are created in cookie."));
    }

    @Test
    @DisplayName("POST /api/auth/login with wrong password should return 401")
    void testLoginFailureRequest() {
        String bodyRequest = """
                {
                    "username" : "%s",
                    "password" : "%s"
                }
                """.formatted(TEST_USER_USERNAME, "wrongPassword");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(401)
                .body("error", equalTo("Unauthorized"));
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid fields should return 400 and validation messages")
    void testLoginUserWithInvalidFields() {
        String bodyRequest = """
                {
                    "username" : "",
                    "password" : ""
                }
                """;

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILURE"))
                .body("username", equalTo("The username is required"))
                .body("password", equalTo("The password is required"));
    }

    @Test
    @DisplayName("POST /api/auth/register should create a new user successfully")
    void testRegisterUserRequest() {
        String bodyRequest = """
                {
                    "email" : "%s",
                    "username" : "%s",
                    "password" : "%s"
                }
                """.formatted("newuser@gmail.com", "newUser", "newPassword");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("message", equalTo("User registered successfully"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 409 if user already exists")
    void testExistingUserRegistration() {
        String bodyRequest = """
                {
                    "email" : "%s",
                    "username" : "%s",
                    "password" : "%s"
                }
                """.formatted(TEST_USER_EMAIL, TEST_USER_USERNAME, "anyPassword");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(409)
                .body("status", equalTo("FAILURE"))
                .body("error", equalTo("User Already Exists in the Database"))
                .body("message", equalTo("The User Already Exists on the Database"));
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid fields should return 400 and validation messages")
    void testRegisterUserWithInvalidFields() {
        String bodyRequest = """
                {
                    "email" : "",
                    "username" : "",
                    "password" : ""
                }
                """;

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILURE"))
                .body("email", equalTo("The email is required"))
                .body("username", equalTo("The username is required"))
                .body("password", equalTo("The password is required"));
    }
}