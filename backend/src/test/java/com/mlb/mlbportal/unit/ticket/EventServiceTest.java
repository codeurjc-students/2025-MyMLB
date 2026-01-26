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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.mlb.mlbportal.dto.ticket.EditEventRequest;
import com.mlb.mlbportal.dto.ticket.EventCreateRequest;
import com.mlb.mlbportal.dto.ticket.EventManagerDTO;
import com.mlb.mlbportal.dto.ticket.EventResponseDTO;
import com.mlb.mlbportal.dto.ticket.SeatDTO;
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
import com.mlb.mlbportal.models.ticket.Seat;
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
    @SuppressWarnings("unused")
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
        assertThat(result.getContent().getFirst().id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should return successfully the event")
    void testGetEvent() {
        EventResponseDTO dto = BuildMocksFactory.buildEventResponseDTO();

        when(this.eventRepository.findEventByIdOrElseThrow(this.testEvent.getId())).thenReturn(this.testEvent);
        when(this.eventMapper.toEventResponseDto(this.testEvent)).thenReturn(dto);

        EventResponseDTO result = this.eventService.getEvent(this.testEvent.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(this.testEvent.getId());
    }

    @Test
    @DisplayName("Should throw EventNotFoundException if the event doesn't exists")
    void testInvalidGetEvent() {
        when(this.eventRepository.findEventByIdOrElseThrow(2L)).thenThrow(new EventNotFoundException(2L));

        assertThatThrownBy(() -> this.eventService.getEvent(2L))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event 2 Not Found");
    }

    @Test
    @DisplayName("Should retrieve available sectors for an event")
    void testGetAvailableSectors() {
        EventManager manager = new EventManager(testEvent, new Sector(), 50.0);
        List<EventManager> managers = List.of(manager);
        Page<EventManagerDTO> mockPage = new PageImpl<>(List.of(mock(EventManagerDTO.class)));

        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);
        when(this.eventManagerRepository.findAvailableSectors(100L)).thenReturn(managers);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(managers), eq(0), eq(10), any());

        Page<EventManagerDTO> result = this.eventService.getAvailableSectors(100L, 0, 10);

        assertThat(result).isNotNull();
        verify(this.eventManagerRepository).findAvailableSectors(100L);
    }

    @Test
    @DisplayName("Should retrieve available seats for a sector in an event")
    void testGetAvailableSeats() {
        List<Seat> seats = List.of(new Seat());
        Page<SeatDTO> mockPage = new PageImpl<>(List.of(new SeatDTO(1L, "A-1")));

        when(this.seatRepository.findAvailableSeats(5L, 100L)).thenReturn(seats);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(seats), eq(0), eq(10), any());

        Page<SeatDTO> result = this.eventService.getAvailableSeats(5L, 100L, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(this.seatRepository).findAvailableSeats(5L, 100L);
    }

    @Test
    @DisplayName("Should retrieve event by match id")
    void testGetEventByMatchId() {
        EventResponseDTO dto = BuildMocksFactory.buildEventResponseDTO();

        when(this.matchRepository.findById(1L)).thenReturn(Optional.of(testMatch));
        when(this.eventRepository.findEventByMatchId(1L)).thenReturn(Optional.of(testEvent));
        when(this.eventMapper.toEventResponseDto(testEvent)).thenReturn(dto);

        EventResponseDTO result = this.eventService.getEventByMatchId(1L);

        assertThat(result.id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should throw MatchNotFoundException if the match doesn't exists")
    void testInvalidGetByMatch() {
        when(this.matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.eventService.getEventByMatchId(99L))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining("Match Not Found");
    }

    @Test
    @DisplayName("Should successfully create an event with its sectors and seats")
    void testCreateEvent() {
        SectorCreateRequest sectorReq = new SectorCreateRequest("Bleachers", 50);
        EventCreateRequest request = new EventCreateRequest(1L, List.of(25.0), List.of(sectorReq));
        EventResponseDTO expectedResponse = BuildMocksFactory.buildEventResponseDTO();

        when(this.matchRepository.findById(1L)).thenReturn(Optional.of(testMatch));
        when(this.eventMapper.toEventResponseDto(any(Event.class))).thenReturn(expectedResponse);

        EventResponseDTO result = this.eventService.createEvent(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);

        verify(this.sectorRepository, times(1)).save(any(Sector.class));
        verify(this.eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw EventRequestMissMatchException when prices and sectors size mismatch")
    void testCreateEventMismatch() {
        EventCreateRequest request = new EventCreateRequest(1L, List.of(10.0, 20.0), List.of(new SectorCreateRequest("S1", 10)));

        assertThatThrownBy(() -> this.eventService.createEvent(request))
                .isInstanceOf(EventRequestMissMatchException.class).
                hasMessageContaining("The number of sectors selected for the creation process does not match the number of new prices");
    }

    @Test
    @DisplayName("Should throw MatchNotFoundException if the match doesn't exists")
    void testCreateEventWithInvalidMatch() {
        when(this.matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.eventService.getEventByMatchId(99L))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining("Match Not Found");
    }

    @Test
    @DisplayName("Should successfully update prices for specific sectors")
    void testEditEventSuccess() {
        long sectorId = 5L;
        Double newPrice = 75.0;

        Sector sector = new Sector("Main Deck", 100, testStadium);
        sector.setId(sectorId);

        EventManager manager = new EventManager(testEvent, sector, 50.0);
        testEvent.setEventManagers(new ArrayList<>(List.of(manager)));

        EditEventRequest request = new EditEventRequest(100L, List.of(sectorId), List.of(newPrice));
        EventResponseDTO expectedResponse = BuildMocksFactory.buildEventResponseDTO();

        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);
        when(this.eventMapper.toEventResponseDto(testEvent)).thenReturn(expectedResponse);

        EventResponseDTO result = this.eventService.editEvent(request);

        assertThat(result).isNotNull();
        assertThat(manager.getPrice()).isEqualTo(newPrice);
        verify(this.eventRepository).save(testEvent);
    }

    @Test
    @DisplayName("Should throw EventRequestMissMatchException when prices and sectors size mismatch")
    void testEditEventMismatch() {
        EditEventRequest request = new EditEventRequest(100L, List.of(22L, 10L), List.of(99.0));

        assertThatThrownBy(() -> this.eventService.editEvent(request))
                .isInstanceOf(EventRequestMissMatchException.class).
                hasMessageContaining("The number of sectors selected for editing does not match the number of new prices");
    }

    @Test
    @DisplayName("Should throw EventNotFoundException if the event doesn't exists")
    void testEditEventInvalidEvent() {
        when(this.eventRepository.findEventByIdOrElseThrow(2L)).thenThrow(new EventNotFoundException(2L));

        assertThatThrownBy(() -> this.eventService.getEvent(2L))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event 2 Not Found");
    }

    @Test
    @DisplayName("Should throw SectorNotFoundException if sector does not belong to event")
    void testEditEventInvalidSector() {
        this.testEvent.setEventManagers(new ArrayList<>());
        EditEventRequest request = new EditEventRequest(100L, List.of(999L), List.of(50.0));

        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);

        assertThatThrownBy(() -> this.eventService.editEvent(request))
                .isInstanceOf(SectorNotFoundException.class)
                .hasMessageContaining("Sector 999 Not Found");
    }

    @Test
    @DisplayName("Should return DTO and call delete")
    void testDeleteEvent() {
        EventResponseDTO dto = BuildMocksFactory.buildEventResponseDTO();
        when(this.eventRepository.findEventByIdOrElseThrow(100L)).thenReturn(testEvent);
        when(this.eventMapper.toEventResponseDto(testEvent)).thenReturn(dto);

        EventResponseDTO result = this.eventService.deleteEvent(100L);

        assertThat(result.id()).isEqualTo(100L);
        verify(this.eventRepository).delete(testEvent);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException if the event doesn't exists")
    void testDeleteEventInvalidEvent() {
        when(this.eventRepository.findEventByIdOrElseThrow(2L)).thenThrow(new EventNotFoundException(2L));

        assertThatThrownBy(() -> this.eventService.getEvent(2L))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event 2 Not Found");
    }
}