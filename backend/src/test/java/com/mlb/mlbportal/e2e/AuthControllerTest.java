package com.mlb.mlbportal.e2e;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mlb.mlbportal.dto.authentication.ForgotPasswordRequest;
import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.authentication.ResetPasswordRequest;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.security.jwt.LoginRequest;
import static com.mlb.mlbportal.utils.TestConstants.FORGOT_PASSWORD_PATH;
import static com.mlb.mlbportal.utils.TestConstants.LOGIN_PATH;
import static com.mlb.mlbportal.utils.TestConstants.LOGOUT_PATH;
import static com.mlb.mlbportal.utils.TestConstants.ME_PATH;
import static com.mlb.mlbportal.utils.TestConstants.NEW_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.REGISTER_PATH;
import static com.mlb.mlbportal.utils.TestConstants.RESET_PASSWORD_PATH;
import static com.mlb.mlbportal.utils.TestConstants.SUCCESS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.VALID_CODE;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest extends BaseE2ETest {

    @MockitoBean
    @SuppressWarnings("unused")
    private JavaMailSender mailSender;

    @Autowired
    private PasswordResetTokenRepository passwordRepository;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        UserEntity user1 = saveTestUser(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return the active user")
    void testGetActiveUser() {
        given()
                .header("X-Mock-User", TEST_USER_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .get(ME_PATH)
                .then()
                .statusCode(200)
                .body("username", is(TEST_USER_USERNAME))
                .body("roles", contains("USER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should authenticate user and return tokens in cookies")
    void testLoginUserRequest() {
        LoginRequest requestBody = new LoginRequest(TEST_USER_USERNAME, TEST_USER_PASSWORD);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(200)
                .body("status", equalTo(SUCCESS))
                .body("message", equalTo("Auth successful. Tokens are created in cookie."));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should create a new user successfully")
    void testRegisterUserRequest() {
        RegisterRequest bodyRequest = new RegisterRequest("newuser@gmail.com", "newUser", "newPassword");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(200)
                .body("status", equalTo(SUCCESS))
                .body("message", equalTo("User registered successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout should logout the active user successfully")
    void testLogout() {
        given()
                .header("X-Mock-User", TEST_USER_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .post(LOGOUT_PATH)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("Logout Successful"));
    }

    @Test
    @DisplayName("DELETE /api/v1/auth should delete the user's account from the system")
    void testDeleteAccount() {
        given()
                .header("X-Mock-User", TEST_USER_USERNAME)
                .contentType(ContentType.JSON)
                .when()
                .delete("api/v1/auth")
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("Account Successfully Deleted"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password should send the recovery email")
    void testForgotPassword() {
        ForgotPasswordRequest request = new ForgotPasswordRequest(TEST_USER_EMAIL);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(FORGOT_PASSWORD_PATH)
                .then()
                .log().all()
                .statusCode(200)
                .body("status", equalTo(SUCCESS))
                .body("message", equalTo("Recovery email sent successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password should reset the password successfully with valid code")
    void testResetPasswordSuccess() {
        UserEntity user = this.userRepository.findByEmail(TEST_USER_EMAIL).orElseThrow();
        PasswordResetToken token = new PasswordResetToken(VALID_CODE, user);
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        this.passwordRepository.save(token);

        ResetPasswordRequest resetRequest = new ResetPasswordRequest(VALID_CODE, NEW_PASSWORD);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(resetRequest)
                .when()
                .post(RESET_PASSWORD_PATH)
                .then()
                .statusCode(200)
                .body("status", equalTo(SUCCESS))
                .body("message", equalTo("Password restored"));
    }
}