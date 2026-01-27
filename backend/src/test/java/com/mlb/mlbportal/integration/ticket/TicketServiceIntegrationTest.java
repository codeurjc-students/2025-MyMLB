package com.mlb.mlbportal.integration.ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.dto.ticket.SeatDTO;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Seat;
import com.mlb.mlbportal.models.ticket.Sector;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.SectorRepository;
import com.mlb.mlbportal.repositories.ticket.TicketRepository;
import com.mlb.mlbportal.services.ticket.TicketService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.USER1_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER1_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketServiceIntegrationTest {
    @Autowired
    private EventManagerRepository eventManagerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private TicketService ticketService;

    private PurchaseRequest validRequest;
    private Seat testSeat;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.ticketRepository.deleteAll();
        this.seatRepository.deleteAll();
        this.eventManagerRepository.deleteAll();
        this.eventRepository.deleteAll();
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();
        this.sectorRepository.deleteAll();
        this.stadiumRepository.deleteAll();
        this.userRepository.deleteAll();

        UserEntity testUser = new UserEntity();
        testUser.setUsername(USER1_USERNAME);
        testUser.setEmail(USER1_EMAIL);
        testUser.setPassword(USER1_PASSWORD);
        this.userRepository.save(testUser);

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.teamRepository.saveAll(teams);
        Team home = teams.get(0);
        Team away = teams.get(1);

        Stadium stadium = new Stadium(STADIUM1_NAME, STADIUM1_YEAR, home);
        stadium = this.stadiumRepository.save(stadium);

        Match match = new Match();
        match.setStadium(stadium);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setDate(LocalDateTime.now());
        match.setStatus(MatchStatus.SCHEDULED);
        match = this.matchRepository.save(match);

        Event event = new Event();
        event.setMatch(match);
        event = this.eventRepository.save(event);

        Sector sector = new Sector("Grandstand", 100, stadium);
        sector = this.sectorRepository.save(sector);

        EventManager testManager = new EventManager(event, sector, 50.0);
        testManager.setAvailability(10);
        testManager = eventManagerRepository.save(testManager);

        this.testSeat = new Seat("A1", sector, false);
        this.testSeat = seatRepository.save(testSeat);

        SeatDTO seatDto = new SeatDTO(this.testSeat.getId(), this.testSeat.getName());
        this.validRequest = new PurchaseRequest(
                testManager.getId(),
                1,
                List.of(seatDto),
                USER1_USERNAME,
                "49927398716",
                "123",
                LocalDate.now().plusYears(1)
        );
    }

    @Test
    @DisplayName("Should purchase the ticket successfully, associates the thickets to the user and set isOccupied to true")
    void testPurchase() {
        this.ticketService.purchaseTicket(USER1_USERNAME, this.validRequest, 0, 10);

        UserEntity userRegistered = this.userRepository.findByUsernameOrThrow(USER1_USERNAME);
        Seat seatRegistered = this.seatRepository.findById(this.testSeat.getId()).orElseThrow();

        assertThat(userRegistered.getTickets()).isNotEmpty();
        assertThat(userRegistered.getTickets()).hasSize(1);
        assertThat(seatRegistered.isOccupied()).isTrue();
    }
}