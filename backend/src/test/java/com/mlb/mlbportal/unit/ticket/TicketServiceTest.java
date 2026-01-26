package com.mlb.mlbportal.unit.ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.dto.ticket.SeatDTO;
import com.mlb.mlbportal.dto.ticket.TicketDTO;
import com.mlb.mlbportal.handler.badRequest.SeatSelectionMismatchException;
import com.mlb.mlbportal.handler.conflict.InsufficientStockException;
import com.mlb.mlbportal.handler.notFound.EventNotFoundException;
import com.mlb.mlbportal.handler.notFound.TicketNotFoundException;
import com.mlb.mlbportal.mappers.ticket.TicketMapper;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Seat;
import com.mlb.mlbportal.models.ticket.Ticket;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.TicketRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.ticket.PaymentService;
import com.mlb.mlbportal.services.ticket.TicketService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import com.mlb.mlbportal.services.utilities.PdfGeneratorService;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.USER1_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;

import jakarta.mail.MessagingException;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private EventManagerRepository eventManagerRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private PaginationHandlerService paginationHandlerService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private EmailService emailService;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private TicketService ticketService;

    private UserEntity testUser;
    private EventManager testManager;
    private PurchaseRequest validRequest;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.testUser = new UserEntity(USER1_EMAIL, USER1_USERNAME);
        this.testManager = new EventManager();
        this.testManager.setId(1L);

        SeatDTO seatDto = new SeatDTO(10L, "A-1");
        this.validRequest = new PurchaseRequest(
                1L, 1, List.of(seatDto), USER1_USERNAME,
                "49927398716", "123", LocalDate.now().plusYears(1)
        );
    }

    @Test
    @DisplayName("Should retrieve the ticket")
    void testGetTicketSuccess() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        TicketDTO dto = new TicketDTO(
                1L, TEST_TEAM1_NAME, TEST_TEAM2_NAME, STADIUM1_NAME,
                50.0, LocalDateTime.now(), "Sector A", "Seat 1"
        );

        when(this.ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(this.ticketMapper.toTicketDTO(ticket)).thenReturn(dto);

        TicketDTO result = this.ticketService.getTicket(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw TicketNotFoundException if ticket does not exist")
    void testGetTicketNotFound() {
        when(this.ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.ticketService.getTicket(1L))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("Ticket 1 Not Found");
    }

    @Test
    @DisplayName("Should retrieve tickets of an event paginated")
    void testGetTicketsOfEventSuccess() {
        Long eventId = 100L;
        List<Ticket> tickets = List.of(new Ticket(), new Ticket());
        TicketDTO dto = mock(TicketDTO.class);
        Page<TicketDTO> mockPage = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(this.eventRepository.findEventByIdOrElseThrow(eventId)).thenReturn(null);
        when(this.ticketRepository.findTicketByEvent(eventId)).thenReturn(tickets);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(tickets), eq(0), eq(10), any());

        Page<TicketDTO> result = this.ticketService.getTicketsOfEvent(eventId, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(this.eventRepository).findEventByIdOrElseThrow(eventId);
        verify(this.ticketRepository).findTicketByEvent(eventId);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when event does not exist")
    void testGetTicketsOfEventNotFound() {
        Long eventId = 999L;
        when(this.eventRepository.findEventByIdOrElseThrow(eventId)).thenThrow(new EventNotFoundException(eventId));

        assertThatThrownBy(() -> this.ticketService.getTicketsOfEvent(eventId, 0, 10))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event 999 Not Found");

        verify(this.ticketRepository, never()).findTicketByEvent(anyLong());
    }

    @Test
    @DisplayName("purchaseTicket should process a full successful purchase")
    void testPurchaseTicketSuccess() throws MessagingException {
        when(this.eventManagerRepository.updateStockAvailability(anyLong(), anyInt())).thenReturn(1);
        when(this.seatRepository.markSeatAsOccupied(anyList())).thenReturn(1);
        when(this.eventManagerRepository.findByIdWithBlock(anyLong())).thenReturn(Optional.of(testManager));
        when(this.userRepository.findByUsernameOrThrow(USER1_USERNAME)).thenReturn(testUser);
        when(this.seatRepository.getReferenceById(anyLong())).thenReturn(new Seat());
        when(this.pdfGeneratorService.generateTicketsPdf(anyList())).thenReturn(new byte[]{1, 2, 3});

        Page<TicketDTO> mockPage = new PageImpl<>(List.of(mock(TicketDTO.class)));
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(anyList(), eq(0), eq(10), any());

        Page<TicketDTO> result = this.ticketService.purchaseTicket(USER1_USERNAME, validRequest, 0, 10);

        assertThat(result).isNotNull();
        verify(this.paymentService, times(1)).processPayment(validRequest);
        verify(this.userRepository, times(1)).save(testUser);
        verify(this.emailService, times(1)).sendTicketPurchaseEmail(eq(USER1_EMAIL), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should throw SeatSelectionMismatchException if seat count differs from ticket amount")
    void testPurchaseTicketMismatch() {
        PurchaseRequest mismatchRequest = new PurchaseRequest(
                1L, 2, List.of(new SeatDTO(1L, "A-1")), USER1_USERNAME,
                "49927398716", "123", LocalDate.now().plusYears(1)
        );

        assertThatThrownBy(() -> this.ticketService.purchaseTicket(USER1_USERNAME, mismatchRequest, 0, 10))
                .isInstanceOf(SeatSelectionMismatchException.class)
                .hasMessageContaining("The amount of selected seats does not match with the total of tickets selected");
    }

    @Test
    @DisplayName("Should throw InsufficientStockException if updateStockAvailability returns 0")
    void testPurchaseNoStock() {
        when(this.eventManagerRepository.updateStockAvailability(anyLong(), anyInt())).thenReturn(0);

        assertThatThrownBy(() -> this.ticketService.purchaseTicket(USER1_USERNAME, validRequest, 0, 10))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Inssuficient Stock");
    }

    @Test
    @DisplayName("Should throw ConcurrentModificationException if seats are already occupied")
    void testPurchaseSeatsTaken() {
        when(this.eventManagerRepository.updateStockAvailability(anyLong(), anyInt())).thenReturn(1);
        when(this.seatRepository.markSeatAsOccupied(anyList())).thenReturn(0);

        assertThatThrownBy(() -> this.ticketService.purchaseTicket(USER1_USERNAME, validRequest, 0, 10))
                .isInstanceOf(ConcurrentModificationException.class)
                .hasMessage("One or more selected seats are no longer available");
    }

    @Test
    @DisplayName("Should handle MessagingException during email sending without failing the purchase")
    void testPurchaseEmailFailure() throws MessagingException {
        when(this.eventManagerRepository.updateStockAvailability(anyLong(), anyInt())).thenReturn(1);
        when(this.seatRepository.markSeatAsOccupied(anyList())).thenReturn(1);
        when(this.eventManagerRepository.findByIdWithBlock(anyLong())).thenReturn(Optional.of(testManager));
        when(this.userRepository.findByUsernameOrThrow(USER1_USERNAME)).thenReturn(testUser);
        when(this.pdfGeneratorService.generateTicketsPdf(anyList())).thenReturn(new byte[]{0});

        doThrow(new MessagingException("Mail server down")).when(this.emailService).sendTicketPurchaseEmail(any(), any(), any(), any(), any());

        doReturn(new PageImpl<>(List.of())).when(this.paginationHandlerService).paginateAndMap(anyList(), anyInt(), anyInt(), any());

        assertThat(this.ticketService.purchaseTicket(USER1_USERNAME, validRequest, 0, 10)).isNotNull();
        verify(this.emailService).sendTicketPurchaseEmail(any(), any(), any(), any(), any());
    }
}