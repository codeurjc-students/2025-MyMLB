package com.mlb.mlbportal.integration.ticket;

import com.mlb.mlbportal.dto.ticket.EditEventRequest;
import com.mlb.mlbportal.dto.ticket.EventCreateRequest;
import com.mlb.mlbportal.dto.ticket.SectorCreateRequest;
import com.mlb.mlbportal.handler.notFound.EventNotFoundException;
import com.mlb.mlbportal.mappers.ticket.SeatMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Sector;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.SectorRepository;
import com.mlb.mlbportal.services.ticket.EventService;
import com.mlb.mlbportal.services.utilities.SeatBatchGenerationService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventServiceIntegrationTest {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventManagerRepository eventManagerRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    @SuppressWarnings("unused")
    private SeatMapper seatMapper;

    @Autowired
    @SuppressWarnings("unused")
    private SeatBatchGenerationService seatBatchGenerationService;

    @Autowired
    private EventService eventService;

    private Event testEvent;
    private Stadium testStadium;
    private Team homeTeam;
    private Team awayTeam;

    @BeforeEach
    void setUp() {
        this.seatRepository.deleteAll();
        this.eventManagerRepository.deleteAll();
        this.eventRepository.deleteAll();
        this.sectorRepository.deleteAll();
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();
        this.stadiumRepository.deleteAll();

        this.testStadium = BuildMocksFactory.setUpStadiums().getFirst();
        this.stadiumRepository.save(this.testStadium);

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.homeTeam = teams.getFirst();
        this.awayTeam = teams.get(1);
        this.teamRepository.save(this.homeTeam);
        this.teamRepository.save(this.awayTeam);

        Match testMatch = new Match();
        testMatch.setStadium(testStadium);
        testMatch.setHomeTeam(this.homeTeam);
        testMatch.setAwayTeam(this.awayTeam);
        testMatch = this.matchRepository.save(testMatch);

        this.testEvent = new Event(testMatch);
        this.eventRepository.save(this.testEvent);
    }

    @Test
    @DisplayName("Should persist event in database after creation")
    void testCreateEvent() {
        Match anotherMatch = new Match();
        anotherMatch.setStadium(this.testStadium);
        anotherMatch.setHomeTeam(this.homeTeam);
        anotherMatch.setAwayTeam(this.awayTeam);
        anotherMatch = this.matchRepository.save(anotherMatch);

        SectorCreateRequest sectorReq = new SectorCreateRequest("Bleachers", 50);
        EventCreateRequest request = new EventCreateRequest(anotherMatch.getId(), List.of(25.0), List.of(sectorReq));

        var result = this.eventService.createEvent(request);

        assertThatCode(() -> {
            Event newEvent = this.eventRepository.findEventByIdOrElseThrow(result.id());
            assertThat(newEvent).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should persist the modifications of the event")
    void testEditEvent() {
        Sector sector = new Sector("Main", 100, this.testStadium);
        this.sectorRepository.save(sector);

        EventManager manager = new EventManager(this.testEvent, sector, 20.0);
        this.eventManagerRepository.save(manager);
        this.testEvent.getEventManagers().add(manager);

        Double newPrice = 50.0;
        EditEventRequest editRequest = new EditEventRequest(this.testEvent.getId(), List.of(sector.getId()), List.of(newPrice));

        this.eventService.editEvent(editRequest);

        assertThatCode(() -> {
            Event editedEvent = this.eventRepository.findEventByIdOrElseThrow(this.testEvent.getId());
            Double priceInDb = editedEvent.getEventManagers().getFirst().getPrice();
            assertThat(priceInDb).isEqualTo(newPrice);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should delete the event from the database")
    void testDeleteEvent() {
        this.eventService.deleteEvent(this.testEvent.getId());
        assertThatThrownBy(() -> this.eventRepository.findEventByIdOrElseThrow(this.testEvent.getId()))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event " + this.testEvent.getId() + " Not Found");
    }
}