package com.mlb.mlbportal.integration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.support.ReplyRequest;
import com.mlb.mlbportal.models.enums.SupportTicketStatus;
import com.mlb.mlbportal.models.support.SupportTicket;
import com.mlb.mlbportal.repositories.SupportMessageRepository;
import com.mlb.mlbportal.repositories.SupportTicketRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.SupportService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.ADMIN_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.SUPPORT_TICKET1_SUBJECT;

import jakarta.mail.MessagingException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SupportServiceIntegrationTests {
    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @MockitoBean
    @SuppressWarnings("unused")
    private EmailService emailService;

    @Autowired
    private SupportService supportService;

    private SupportTicket openTicket;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.supportMessageRepository.deleteAll();
        this.supportTicketRepository.deleteAll();

        this.openTicket = BuildMocksFactory.buildSupportTicket(null, SUPPORT_TICKET1_SUBJECT, SupportTicketStatus.OPEN);

        this.openTicket = this.supportTicketRepository.save(this.openTicket);
    }

    @Test
    @DisplayName("Should answer to a ticket and change its status to 'ANSWERED'")
    void testReply() throws MessagingException {
        ReplyRequest request = new ReplyRequest(ADMIN_EMAIL, "Reply body");

        this.supportService.reply(this.openTicket.getId(), request);

        SupportTicket updatedTicket = this.supportTicketRepository.findById(this.openTicket.getId()).orElseThrow();

        assertThat(updatedTicket.getStatus()).isEqualTo(SupportTicketStatus.ANSWERED);
    }

    @Test
    @DisplayName("Should close a ticket and change its status to 'CLOSED")
    void testCloseTicket() {
        this.supportService.closeTicket(this.openTicket.getId());

        SupportTicket updatedTicket = this.supportTicketRepository.findById(this.openTicket.getId()).orElseThrow();

        assertThat(updatedTicket.getStatus()).isEqualTo(SupportTicketStatus.CLOSED);
    }
}