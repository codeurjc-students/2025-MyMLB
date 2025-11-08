package com.mlb.mlbportal.e2e;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
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
import static com.mlb.mlbportal.utils.TestConstants.FAILURE;
import static com.mlb.mlbportal.utils.TestConstants.FORGOT_PASSWORD_PATH;
import static com.mlb.mlbportal.utils.TestConstants.INVALID_CODE;
import static com.mlb.mlbportal.utils.TestConstants.INVALID_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.LOGIN_PATH;
import static com.mlb.mlbportal.utils.TestConstants.NEW_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.REGISTER_PATH;
import static com.mlb.mlbportal.utils.TestConstants.RESET_PASSWORD_PATH;
import static com.mlb.mlbportal.utils.TestConstants.SUCCESS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_EMAIL;
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
        saveTestUser(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);
    }

    @Test
    @DisplayName("POST /api/auth/login should authenticate user and return tokens in cookies")
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
    @DisplayName("POST /api/auth/login with wrong password should return 401")
    void testLoginFailureRequest() {
        LoginRequest requestBody = new LoginRequest(TEST_USER_USERNAME, "wrongPassword");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(401)
                .body("error", equalTo("Unauthorized"));
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid fields should return 400 and validation messages")
    void testLoginUserWithInvalidFields() {
        LoginRequest requestBody = new LoginRequest("", "");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(FAILURE))
                .body("username", equalTo("The username is required"))
                .body("password", equalTo("The password is required"));
    }

    @Test
    @DisplayName("POST /api/auth/register should create a new user successfully")
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
    @DisplayName("POST /api/auth/register should return 409 if user already exists")
    void testExistingUserRegistration() {
        RegisterRequest bodyRequest = new RegisterRequest(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(409)
                .body("status", equalTo(FAILURE))
                .body("error", equalTo("User Already Exists"))
                .body("message", equalTo("The User Already Exists in the Database"));
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid fields should return 400 and validation messages")
    void testRegisterUserWithInvalidFields() {
        RegisterRequest bodyRequest = new RegisterRequest("", "", "");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(bodyRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(FAILURE))
                .body("email", equalTo("The email is required"))
                .body("username", equalTo("The username is required"))
                .body("password", equalTo("The password is required"));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password should send the recovery email")
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
    @DisplayName("POST /api/auth/forgot-password with an invalid email format should return a 400 status code")
    void testForgotPasswordWithInvalidInvalidRequest() {
        ForgotPasswordRequest request = new ForgotPasswordRequest(INVALID_EMAIL);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(FORGOT_PASSWORD_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(FAILURE))
                .body("email", equalTo("Invalid email format"));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password with an empty email should return a 400 status code")
    void testForgotPasswordWithEmptyEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("");

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(FORGOT_PASSWORD_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(FAILURE))
                .body("email", equalTo("Email is required"));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password should return 404 if the email is not registered")
    void testForgotPasswordEmailNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest(UNKNOWN_EMAIL);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(FORGOT_PASSWORD_PATH)
                .then()
                .statusCode(404)
                .body("status", equalTo(FAILURE))
                .body("message", equalTo("There is no user registered with this email"))
                .body("error", equalTo("User Not Found"));
    }

    private void setUpResetPasswordMethod(boolean isValid) {
        UserEntity user = this.userRepository.findByEmail(TEST_USER_EMAIL).orElseThrow();
        PasswordResetToken token;

        if (isValid) {
            token = new PasswordResetToken(VALID_CODE, user);
        } else {
            token = new PasswordResetToken(VALID_CODE, user);
        }
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        this.passwordRepository.save(token);
    }

    @Test
    @DisplayName("POST /api/auth/reset-password should reset the password successfully with valid code")
    void testResetPasswordSuccess() {
        this.setUpResetPasswordMethod(true);

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

    @Test
    @DisplayName("POST /api/auth/reset-password should return 400 if code is invalid")
    void testResetPasswordInvalidCode() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(INVALID_CODE, NEW_PASSWORD);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(resetRequest)
                .when()
                .post(RESET_PASSWORD_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(FAILURE))
                .body("message", equalTo("Invalid or expired code"));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password should return 400 if code is expired")
    void testResetPasswordExpiredCode() {
        this.setUpResetPasswordMethod(false);

        ResetPasswordRequest resetRequest = new ResetPasswordRequest("0000", NEW_PASSWORD);

        given()
                .baseUri(RestAssured.baseURI)
                .port(this.port)
                .contentType(ContentType.JSON)
                .body(resetRequest)
                .when()
                .post(RESET_PASSWORD_PATH)
                .then()
                .statusCode(400)
                .body("status", equalTo(FAILURE))
                .body("message", equalTo("Invalid or expired code"));
    }
}