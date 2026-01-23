package com.mlb.mlbportal.e2e.support;

import static com.mlb.mlbportal.utils.TestConstants.*;

import com.mlb.mlbportal.dto.support.ReplyRequest;
import com.mlb.mlbportal.e2e.BaseE2ETest;
import com.mlb.mlbportal.models.enums.SupportTicketStatus;
import com.mlb.mlbportal.models.support.SupportMessage;
import com.mlb.mlbportal.models.support.SupportTicket;
import com.mlb.mlbportal.repositories.SupportMessageRepository;
import com.mlb.mlbportal.repositories.SupportTicketRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminSupportControllerTest extends BaseE2ETest {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @MockitoBean
    @SuppressWarnings("unused")
    private EmailService emailService;

    private SupportTicket savedTicket;

    @BeforeEach
    void setUp() {
        this.supportMessageRepository.deleteAll();
        this.supportTicketRepository.deleteAll();

        SupportTicket ticket = BuildMocksFactory.buildSupportTicket(null, SUPPORT_TICKET1_SUBJECT, SupportTicketStatus.OPEN);

        this.savedTicket = this.supportTicketRepository.save(ticket);

        SupportMessage message = BuildMocksFactory.buildSupportMessage(this.savedTicket);
        this.supportMessageRepository.save(message);
    }

    @Test
    @DisplayName("GET /api/v1/admin/support/tickets should return all open tickets")
    void testGetOpenTickets() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(ADMIN_SUPPORT_PATH)
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("GET /api/v1/admin/support/tickets/{ticketId}/conversation should return the messages")
    void testGetConversation() {
        String url = ADMIN_SUPPORT_PATH + "/" + savedTicket.getId() + "/conversation";

        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].body", notNullValue());
    }

    @Test
    @DisplayName("POST /api/v1/admin/support/tickets/{ticketId}/reply should send the reply to the user")
    void testReply() {
        String url = ADMIN_SUPPORT_PATH + "/" + savedTicket.getId() + "/reply";

        given()
                .contentType(ContentType.JSON)
                .body(new ReplyRequest(USER1_EMAIL, SUPPORT_MESSAGE_BODY))
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("body", notNullValue());
    }

    @Test
    @DisplayName("POST /api/v1/admin/support/tickets/{ticketId}/close should close the ticket")
    void testCloseTicket() {
        String url = ADMIN_SUPPORT_PATH + "/" + savedTicket.getId() + "/close";

        given()
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .body("status", is(SUCCESS))
                .body("message", is("Ticket Successfully Closed"));
    }
}