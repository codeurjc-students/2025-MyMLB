package com.mlb.mlbportal.e2e.ticket;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.dto.ticket.SeatDTO;
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
import com.mlb.mlbportal.models.ticket.Ticket;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.SectorRepository;
import com.mlb.mlbportal.repositories.ticket.TicketRepository;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TICKET_PATH;
import static com.mlb.mlbportal.utils.TestConstants.USER1_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER1_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TicketControllerTest extends BaseE2ETest {

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
    private TicketRepository ticketRepository;

    private Long savedEventManagerId;
    private Long savedSeatId;
    private Long seatIdForPurchase;
    private String savedSeatName;
    private String seatNameForPurchase;
    private Ticket ticket;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        cleanDatabase();

        saveTestUser(USER1_EMAIL, USER1_USERNAME, USER1_PASSWORD);

        Team home = saveTestTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, TEST_TEAM1_CITY, "Info", Collections.emptyList(), League.AL, Division.EAST);
        Team away = saveTestTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, TEST_TEAM2_CITY, "Info", Collections.emptyList(), League.NL, Division.WEST);

        Stadium stadium = this.stadiumRepository.save(new Stadium(STADIUM1_NAME, STADIUM1_YEAR, home));

        Match match = new Match(home, away, 0, 0, LocalDateTime.now().plusDays(1), MatchStatus.SCHEDULED);
        match.setStadium(stadium);
        match = this.matchRepository.save(match);

        Event event = this.eventRepository.save(new Event(match));
        Sector sector = this.sectorRepository.save(new Sector("Premium", 50, stadium));

        EventManager manager = new EventManager(event, sector, 150.0);
        manager.setAvailability(10);
        manager = this.eventManagerRepository.save(manager);
        this.savedEventManagerId = manager.getId();

        Seat seat = this.seatRepository.save(new Seat("B12", sector, true));
        this.savedSeatId = seat.getId();
        this.savedSeatName = seat.getName();

        this.ticket = new Ticket();
        this.ticket.setOwnerName(USER1_USERNAME);
        this.ticket.setSeat(seat);
        this.ticket.setEventManager(manager);
        this.ticket = this.ticketRepository.save(this.ticket);

        Seat seat2 = this.seatRepository.save(new Seat("C05", sector, false));
        this.seatIdForPurchase = seat2.getId();
        this.seatNameForPurchase = seat2.getName();
    }

    @Test
    @DisplayName("GET /api/v1/tickets/{id} should return ticket details")
    void testGetTicketById() {
        SeatDTO seatDto = new SeatDTO(this.savedSeatId, this.savedSeatName);
        PurchaseRequest request = new PurchaseRequest(this.savedEventManagerId, 1, List.of(seatDto), USER1_USERNAME, "4539148912345674", "123",  YearMonth.now().plusYears(1));
        String url = TICKET_PATH + "/" + this.ticket.getId();

        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("ownerName", is(this.ticket.getOwner()));
    }

    @Test
    @DisplayName("POST /api/v1/tickets should purchase a ticket successfully")
    void testPurchaseTicket() {
        SeatDTO seatDto = new SeatDTO(this.seatIdForPurchase, this.seatNameForPurchase);
        PurchaseRequest request = new PurchaseRequest(
                this.savedEventManagerId,
                1,
                List.of(seatDto),
                USER1_USERNAME,
                "4539148912345674",
                "123",
                YearMonth.now().plusYears(2)
        );

        given()
                .header("X-Mock-User", USER1_USERNAME)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TICKET_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(TICKET_PATH))
                .body("content", hasSize(1))
                .body("content[0].seatName", is(this.seatNameForPurchase));
    }
}