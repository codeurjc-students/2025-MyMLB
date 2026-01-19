package com.mlb.mlbportal.e2e.support;

import static com.mlb.mlbportal.utils.TestConstants.*;

import com.mlb.mlbportal.dto.support.CreateTicketRequest;
import com.mlb.mlbportal.e2e.BaseE2ETest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SupportControllerTest extends BaseE2ETest {
    @Test
    @DisplayName("POST /api/v1/support should create a ticket")
    void testCreateTicket() {
        given()
                .contentType(ContentType.JSON)
                .body(new CreateTicketRequest(USER1_EMAIL, SUPPORT_TICKET1_SUBJECT, SUPPORT_MESSAGE_BODY))
                .when()
                .post(SUPPORT_PATH)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("Ticket Successfully Created"));
    }
}