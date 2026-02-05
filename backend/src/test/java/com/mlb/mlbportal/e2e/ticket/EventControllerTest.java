package com.mlb.mlbportal.e2e.ticket;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import com.mlb.mlbportal.repositories.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.ticket.EditEventRequest;
import com.mlb.mlbportal.dto.ticket.EventCreateRequest;
import com.mlb.mlbportal.dto.ticket.SectorCreateRequest;
import com.mlb.mlbportal.e2e.BaseE2ETest;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Seat;
import com.mlb.mlbportal.models.ticket.Sector;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.SectorRepository;
import static com.mlb.mlbportal.utils.TestConstants.EVENT_PATH;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EventControllerTest extends BaseE2ETest {

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private EventManagerRepository eventManagerRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TeamRepository teamRepository;

    private Long savedEventId;
    private Long savedSectorId;

    private Team homeTeam;
    private Team awayTeam;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();
        this.homeTeam = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, 10, 5, TEST_TEAM1_CITY, "Info1", Collections.emptyList(), League.AL, Division.EAST);
        this.awayTeam = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, 8, 7, TEST_TEAM2_CITY, "Info2", Collections.emptyList(), League.NL, Division.WEST);

        Stadium stadium = new Stadium(STADIUM1_NAME, STADIUM1_YEAR, this.homeTeam);
        stadium = this.stadiumRepository.saveAndFlush(stadium);

        this.homeTeam.setStadium(stadium);
        this.teamRepository.saveAndFlush(this.homeTeam);
        this.teamRepository.saveAndFlush(this.awayTeam);

        Match match = new Match(this.awayTeam, this.homeTeam, 0, 0, LocalDateTime.now().plusDays(1), MatchStatus.SCHEDULED);
        match.setStadium(stadium);
        match = this.matchRepository.saveAndFlush(match);

        Event event = new Event(match);
        event = this.eventRepository.saveAndFlush(event);
        this.savedEventId = event.getId();

        Sector sector = new Sector("Grandstand", 100, stadium);
        sector = this.sectorRepository.saveAndFlush(sector);
        this.savedSectorId = sector.getId();

        EventManager manager = new EventManager(event, sector, 50.0);
        manager.setAvailability(10);
        this.eventManagerRepository.saveAndFlush(manager);

        Seat seat = new Seat("A1", sector, false);
        this.seatRepository.saveAndFlush(seat);
    }

    @Test
    @DisplayName("GET /api/v1/events should return all active events")
    void testGetAllEvents() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(EVENT_PATH)
                .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1))
                .body("content[0].homeTeamName", is(TEST_TEAM1_NAME))
                .body("content[0].awayTeamName", is(TEST_TEAM2_NAME));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} should return specific event")
    void testGetEventById() {
        String url = EVENT_PATH + "/" + savedEventId;
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("id", is(savedEventId.intValue()))
                .body("stadiumName", is(STADIUM1_NAME));
    }

    @Test
    @DisplayName("GET /api/v1/events/match/{matchId} should return event by match ID")
    void testGetEventByMatchId() {
        Long matchId = eventRepository.findById(savedEventId).get().getMatch().getId();

        String url = EVENT_PATH + "/match/" + matchId;
        given()
                .accept(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("id", is(savedEventId.intValue()))
                .body("homeTeamName", is(TEST_TEAM1_NAME));
    }

    @Test
    @DisplayName("GET /api/v1/events/{eventId}/tickets should return sold tickets")
    void testGetTicketsOfEvent() {
        String url = EVENT_PATH + "/" + savedEventId + "/tickets";
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("content.size()", is(0));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id}/sectors should return available sectors")
    void testGetAvailableSectors() {
        String url = EVENT_PATH + "/" + savedEventId + "/sectors";
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("content.size()", is(1))
                .body("content[0].sectorName", is("Grandstand"))
                .body("content[0].price", is(50.0f));
    }

    @Test
    @DisplayName("GET /api/v1/events/{eventId}/sector/{sectorId} should return available seats")
    void testGetAvailableSeats() {
        String url = EVENT_PATH + "/" + savedEventId + "/sector/" + savedSectorId;
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("content.size()", is(1))
                .body("content[0].name", is("A1"));
    }

    @Test
    @DisplayName("POST /api/v1/events should create a new event")
    void testCreateEvent() {
        Stadium stadium = new Stadium("New Stadium", 2024, this.homeTeam);
        stadium = this.stadiumRepository.save(stadium);

        this.sectorRepository.save(new Sector("Bleachers", 50, stadium));

        Match newMatch = new Match(this.awayTeam, this.homeTeam, 0, 0, LocalDateTime.now(), MatchStatus.SCHEDULED);
        newMatch.setStadium(stadium);
        newMatch = this.matchRepository.save(newMatch);

        SectorCreateRequest sectorCreateRequest = new SectorCreateRequest("Bleachers", 50);

        EventCreateRequest request = new EventCreateRequest(
                newMatch.getId(),
                List.of(25.0),
                List.of(sectorCreateRequest)
        );

        String headerLocation = EVENT_PATH + "/";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(EVENT_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(headerLocation))
                .body("homeTeamName", is(this.homeTeam.getName()));
    }

    @Test
    @DisplayName("PUT /api/v1/events should edit event sector prices")
    void testEditEvent() {
        EditEventRequest request = new EditEventRequest(
                savedEventId,
                List.of(savedSectorId),
                List.of(99.99)
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(EVENT_PATH)
                .then()
                .statusCode(200)
                .body("id", is(savedEventId.intValue()));
    }

    @Test
    @DisplayName("DELETE /api/v1/events/{id} should remove an event")
    void testDeleteEvent() {
        String url = EVENT_PATH + "/" + savedEventId;
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .statusCode(200)
                .body("id", is(savedEventId.intValue()));
    }
}