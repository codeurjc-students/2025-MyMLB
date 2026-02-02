package com.mlb.mlbportal.unit.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.mlb.mlbportal.dto.ticket.EditEventRequest;
import com.mlb.mlbportal.dto.ticket.EventCreateRequest;
import com.mlb.mlbportal.dto.ticket.EventResponseDTO;
import com.mlb.mlbportal.dto.ticket.SectorCreateRequest;
import com.mlb.mlbportal.handler.badRequest.EventRequestMissMatchException;
import com.mlb.mlbportal.handler.notFound.EventNotFoundException;
import com.mlb.mlbportal.handler.notFound.MatchNotFoundException;
import com.mlb.mlbportal.handler.notFound.SectorNotFoundException;
import com.mlb.mlbportal.mappers.ticket.EventMapper;
import com.mlb.mlbportal.mappers.ticket.SeatMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Sector;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.SectorRepository;
import com.mlb.mlbportal.services.ticket.EventService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import com.mlb.mlbportal.services.utilities.SeatBatchGenerationService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventManagerRepository eventManagerRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    @SuppressWarnings("unused")
    private SeatMapper seatMapper;

    @Mock
    private PaginationHandlerService paginationHandlerService;

    @Mock
    @SuppressWarnings("unused")
    private SeatBatchGenerationService seatBatchGenerationService;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private Match testMatch;
    private Stadium testStadium;

    @BeforeEach
    void setUp() {
        this.testStadium = BuildMocksFactory.setUpStadiums().getFirst();
        this.testMatch = new Match();
        this.testMatch.setId(1L);
        this.testMatch.setStadium(testStadium);
        this.testEvent = new Event(testMatch);
        this.testEvent.setId(100L);
    }

    @Test
    @DisplayName("Should retrieve all events paginated")
    void testGetAllEvents() {
        List<Event> events = List.of(this.testEvent);
        EventResponseDTO dto = BuildMocksFactory.buildEventResponseDTO();
        Page<EventResponseDTO> mockPage = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(this.eventRepository.findAll()).thenReturn(events);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(events), eq(0), eq(10), any());

        Page<EventResponseDTO> result = this.eventService.getAllEvents(0, 10);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return successfully the event")
    void testGetEvent() {
        EventResponseDTO dto = BuildMocksFactory.buildEventResponseDTO();
        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(this.testEvent);
        when(this.eventMapper.toEventResponseDto(this.testEvent)).thenReturn(dto);

        EventResponseDTO result = this.eventService.getEvent(100L);
        assertThat(result.id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should retrieve available sectors for an event")
    void testGetAvailableSectors() {
        EventManager manager = new EventManager(testEvent, new Sector(), 50.0);
        List<EventManager> managers = List.of(manager);
        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);
        when(this.eventManagerRepository.findAvailableSectors(100L)).thenReturn(managers);
        doReturn(new PageImpl<>(List.of())).when(this.paginationHandlerService).paginateAndMap(any(), any(Integer.class), any(Integer.class), any());

        this.eventService.getAvailableSectors(100L, 0, 10);
        verify(this.eventManagerRepository).findAvailableSectors(100L);
    }

    @Test
    @DisplayName("Should successfully create an event with its sectors and seats")
    void testCreateEvent() {
        SectorCreateRequest sectorReq = new SectorCreateRequest("Bleachers", 50);
        EventCreateRequest request = new EventCreateRequest(1L, List.of(25.0), List.of(sectorReq));
        when(this.matchRepository.findById(1L)).thenReturn(Optional.of(testMatch));
        when(this.eventMapper.toEventResponseDto(any(Event.class))).thenReturn(BuildMocksFactory.buildEventResponseDTO());

        this.eventService.createEvent(request);
        verify(this.eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should successfully update prices for specific sectors")
    void testEditEventSuccess() {
        long sectorId = 5L;
        Sector sector = new Sector("Main", 100, testStadium);
        sector.setId(sectorId);
        EventManager manager = new EventManager(testEvent, sector, 50.0);
        testEvent.setEventManagers(new ArrayList<>(List.of(manager)));

        EditEventRequest request = new EditEventRequest(100L, List.of(sectorId), List.of(75.0));
        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);
        when(this.eventMapper.toEventResponseDto(testEvent)).thenReturn(BuildMocksFactory.buildEventResponseDTO());

        this.eventService.editEvent(request);
        assertThat(manager.getPrice()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Should delete the event and return it")
    void testDeleteEvent() {
        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);
        when(this.eventMapper.toEventResponseDto(testEvent)).thenReturn(BuildMocksFactory.buildEventResponseDTO());

        this.eventService.deleteEvent(100L);
        verify(this.eventRepository).delete(testEvent);
    }

    @ParameterizedTest(name = "Operation {0} should throw EventNotFoundException")
    @ValueSource(strings = {"GET", "EDIT", "DELETE"})
    @DisplayName("Should throw EventNotFoundException in all relevant operations")
    void testEventNotFoundParameterized(String operation) {
        long invalidId = 2L;
        when(this.eventRepository.findEventByIdOrElseThrow(invalidId)).thenThrow(new EventNotFoundException(invalidId));

        Runnable action = switch (operation) {
            case "GET" -> () -> eventService.getEvent(invalidId);
            case "EDIT" -> () -> eventService.editEvent(new EditEventRequest(invalidId, List.of(1L), List.of(1.0)));
            case "DELETE" -> () -> eventService.deleteEvent(invalidId);
            default -> throw new IllegalArgumentException("Invalid operation");
        };

        assertThatThrownBy(action::run)
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event " + invalidId + " Not Found");
    }

    @ParameterizedTest(name = "Operation {0} should throw MatchNotFoundException")
    @ValueSource(strings = {"GET_BY_MATCH", "CREATE"})
    @DisplayName("Should throw MatchNotFoundException when match is missing")
    void testMatchNotFoundParameterized(String operation) {
        long invalidMatchId = 99L;
        when(this.matchRepository.findById(invalidMatchId)).thenReturn(Optional.empty());

        Runnable action = switch (operation) {
            case "GET_BY_MATCH" -> () -> eventService.getEventByMatchId(invalidMatchId);
            case "CREATE" -> {
                EventCreateRequest req = new EventCreateRequest(invalidMatchId, List.of(), List.of());
                yield () -> eventService.createEvent(req);
            }
            default -> throw new IllegalArgumentException("Unsupported operation");
        };

        assertThatThrownBy(action::run)
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining("Match Not Found");
    }

    @Test
    @DisplayName("Should throw EventRequestMissMatchException when prices and sectors size mismatch")
    void testCreateEventMismatch() {
        EventCreateRequest request = new EventCreateRequest(1L, List.of(10.0, 20.0), List.of(new SectorCreateRequest("S1", 10)));
        assertThatThrownBy(() -> this.eventService.createEvent(request))
                .isInstanceOf(EventRequestMissMatchException.class);
    }

    @Test
    @DisplayName("Should throw SectorNotFoundException if sector does not belong to event")
    void testEditEventInvalidSector() {
        this.testEvent.setEventManagers(new ArrayList<>());
        EditEventRequest request = new EditEventRequest(100L, List.of(999L), List.of(50.0));
        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);

        assertThatThrownBy(() -> this.eventService.editEvent(request))
                .isInstanceOf(SectorNotFoundException.class);
    }
}