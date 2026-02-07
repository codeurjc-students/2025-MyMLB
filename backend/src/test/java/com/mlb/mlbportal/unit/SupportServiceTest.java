package com.mlb.mlbportal.unit;

import static org.assertj.core.api.Assertions.*;

import com.mlb.mlbportal.dto.support.CreateTicketRequest;
import com.mlb.mlbportal.dto.support.ReplyRequest;
import com.mlb.mlbportal.dto.support.SupportMessageDTO;
import com.mlb.mlbportal.dto.support.SupportTicketDTO;
import com.mlb.mlbportal.handler.conflict.SupportTicketAlreadyIsClosedException;
import com.mlb.mlbportal.handler.notFound.SupportTicketNotFoundException;
import com.mlb.mlbportal.mappers.SupportMessageMapper;
import com.mlb.mlbportal.mappers.SupportTicketMapper;
import com.mlb.mlbportal.models.enums.SupportTicketStatus;
import com.mlb.mlbportal.models.support.SupportMessage;
import com.mlb.mlbportal.models.support.SupportTicket;
import com.mlb.mlbportal.repositories.SupportMessageRepository;
import com.mlb.mlbportal.repositories.SupportTicketRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.SupportService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {
    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private SupportMessageRepository supportMessageRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SupportTicketMapper supportTicketMapper;

    @Mock
    private SupportMessageMapper supportMessageMapper;

    @InjectMocks
    private SupportService supportService;

    private SupportTicket openTicket;
    private SupportTicket closeTicket;
    private SupportMessage message1;
    private SupportMessage message2;

    @BeforeEach
    void setUp() {
        this.openTicket = BuildMocksFactory.buildSupportTicket(SUPPORT_TICKET1_ID, SUPPORT_TICKET1_SUBJECT, SupportTicketStatus.OPEN);
        this.closeTicket = BuildMocksFactory.buildSupportTicket(SUPPORT_TICKET2_ID, SUPPORT_TICKET2_SUBJECT,SupportTicketStatus.CLOSED);

        this.message1 = BuildMocksFactory.buildSupportMessage(this.openTicket);
        this.message2 = BuildMocksFactory.buildSupportMessage(this.closeTicket);

        this.openTicket.setMessages(new ArrayList<>(List.of(this.message1)));
        this.closeTicket.setMessages(new ArrayList<>(List.of(this.message2)));
    }

    @Test
    @DisplayName("Should create a new ticket")
    void testCreateSupportTicket() {
        CreateTicketRequest request = new CreateTicketRequest(USER1_EMAIL, SUPPORT_TICKET1_SUBJECT, SUPPORT_MESSAGE_BODY);

        this.supportService.createSupportTicket(request.email(), request);

        verify(supportTicketRepository, times(1)).save(any(SupportTicket.class));
        verify(supportMessageRepository, times(1)).save(any(SupportMessage.class));
    }

    @Test
    @DisplayName("Should return all open tickets")
    void testGetAllSupportTickets() {
        List<SupportTicket> openTickets = List.of(this.openTicket);
        SupportTicketDTO dto = new SupportTicketDTO(SUPPORT_TICKET1_ID, SUPPORT_TICKET1_SUBJECT, USER1_EMAIL, SupportTicketStatus.OPEN, LocalDateTime.now());
        List<SupportTicketDTO> openTicketsDTO = List.of(dto);

        when(this.supportTicketRepository.findAllOpenTickets()).thenReturn(openTickets);
        when(this.supportTicketMapper.toListSupportThreadDTO(openTickets)).thenReturn(openTicketsDTO);

        List<SupportTicketDTO> result = this.supportService.getOpenTickets();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(SUPPORT_TICKET1_ID);
    }

    @Test
    @DisplayName("Should return the conversation of a certain ticket between the user and the admin")
    void testGetConversation() {
        List<SupportMessage> messages = List.of(this.message1);
        List<SupportMessageDTO> dtoList = List.of(mock(SupportMessageDTO.class));

        when(this.supportMessageRepository.findBySupportTicketIdOrderByCreationDateAsc(SUPPORT_TICKET1_ID)).thenReturn(messages);
        when(this.supportMessageMapper.toListSupportMessageDTO(messages)).thenReturn(dtoList);

        List<SupportMessageDTO> result = this.supportService.getConversation(SUPPORT_TICKET1_ID);

        assertThat(dtoList).isEqualTo(result);
    }

    @Test
    @DisplayName("Should reply to the user successfully")
    void testReply() throws MessagingException {
        ReplyRequest request = new ReplyRequest(ADMIN_EMAIL, "Reply body");
        SupportMessageDTO dto = new SupportMessageDTO(3L, ADMIN_EMAIL, "Reply body", false, LocalDateTime.now());

        when(this.supportTicketRepository.findById(SUPPORT_TICKET1_ID)).thenReturn(Optional.of(this.openTicket));
        when(this.supportMessageMapper.toSupportMessageDTO(any(SupportMessage.class))).thenReturn(dto);

        this.supportService.reply(SUPPORT_TICKET1_ID, request);

        verify(this.supportMessageRepository).save(any(SupportMessage.class));
        verify(this.supportTicketRepository).save(this.openTicket);
        verify(this.emailService).answerToUser(eq(this.openTicket.getUserEmail()), anyString(), eq(request.body()));
    }

    @Test
    @DisplayName("Should throw SupportTicketAlreadyIsClosedException when attempting to answer a ticket that is close")
    void testInvalidReply() {
        ReplyRequest request = new ReplyRequest(ADMIN_EMAIL, "Reply body");

        when(this.supportTicketRepository.findById(SUPPORT_TICKET2_ID)).thenReturn(Optional.of(this.closeTicket));

        assertThatThrownBy(() -> this.supportService.reply(SUPPORT_TICKET2_ID, request))
                .isInstanceOf(SupportTicketAlreadyIsClosedException.class)
                .hasMessageContaining("Support Ticket already been closed");
    }

    @Test
    @DisplayName("Should throw SupportTicketNotFound when the ticket doesn't exist")
    void testReplyWithNonExistentTicket() {
        ReplyRequest request = new ReplyRequest(ADMIN_EMAIL, "Reply body");
        Long nonExistentId = 99L;

        when(this.supportTicketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.supportService.reply(nonExistentId, request))
                .isInstanceOf(SupportTicketNotFoundException.class)
                .hasMessageContaining("Ticket Not Found");
    }

    @Test
    @DisplayName("Should close the given ticket successfully")
    void testCloseTicket() {
        when(this.supportTicketRepository.findById(SUPPORT_TICKET1_ID)).thenReturn(Optional.of(this.openTicket));

        this.supportService.closeTicket(SUPPORT_TICKET1_ID);

        verify(this.supportTicketRepository, times(1)).save(this.openTicket);
    }

    @Test
    @DisplayName("Should throw SupportTicketNotFound when closing a non-existent ticket")
    void testCloseNonExistentTicket() {
        Long nonExistentId = 99L;

        when(this.supportTicketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.supportService.closeTicket(nonExistentId))
                .isInstanceOf(SupportTicketNotFoundException.class)
                .hasMessageContaining("Ticket Not Found");
    }


    @Test
    @DisplayName("Should throw SupportTicketAlreadyIsClosedException when attempting to close a ticket that is close")
    void testInvalidCloseTicket() {
        when(this.supportTicketRepository.findById(SUPPORT_TICKET2_ID)).thenReturn(Optional.of(this.closeTicket));

        assertThatThrownBy(() -> this.supportService.closeTicket(SUPPORT_TICKET2_ID))
                .isInstanceOf(SupportTicketAlreadyIsClosedException.class)
                .hasMessageContaining("Support Ticket already been closed");
    }
}