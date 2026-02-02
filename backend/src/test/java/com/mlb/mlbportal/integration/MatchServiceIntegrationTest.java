package com.mlb.mlbportal.integration;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchServiceIntegrationTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private Clock clock;

    private LocalDateTime now;

    private Team team1, team2, team3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();
        this.userRepository.deleteAll();

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.team1 = this.teamRepository.save(teams.getFirst());
        this.team2 = this.teamRepository.save(teams.get(1));
        this.team3 = this.teamRepository.save(teams.get(2));

        this.now = LocalDateTime.now(this.clock);
    }

    @Test
    @DisplayName("Should only return matches scheduled for today")
    void testMatchesOfTheDayOnlyToday() {
        Match todayMatch = new Match(this.team1, this.team2, 1, 1, this.now.minusMinutes(5), MatchStatus.SCHEDULED);
        Match yesterdayMatch = new Match(this.team1, this.team2, 2, 2, this.now.minusDays(1), MatchStatus.SCHEDULED);
        this.matchRepository.saveAll(List.of(todayMatch, yesterdayMatch));

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(null, 0, 10);
        MatchDTO result = resultPage.getContent().getFirst();

        assertThat(result.date().toLocalDate()).isEqualTo(this.now.toLocalDate());
    }

    @Test
    @DisplayName("Should prioritize matches with user's favorite teams")
    void testMatchesOfTheDayFavoriteTeamOrdering() {
        Match match1 = new Match(this.team1, this.team2, 1, 1, this.now.minusMinutes(5), MatchStatus.SCHEDULED);
        Match match2 = new Match(this.team3, this.team1, 2, 2, this.now.minusMinutes(5), MatchStatus.SCHEDULED);
        this.matchRepository.saveAll(List.of(match1, match2));

        UserEntity user = BuildMocksFactory.setUpUsers().getLast();
        user.setFavTeams(Set.of(this.team3));
        this.userRepository.save(user);

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(TEST_USER_USERNAME, 0, 10);
        MatchDTO result = resultPage.getContent().getFirst();

        assertThat(result.homeTeam().name()).isEqualTo(this.team1.getName());
        assertThat(result.awayTeam().name()).isEqualTo(this.team3.getName());
    }

    @Test
    @DisplayName("Should not prioritize matches when username is null")
    void testMatchesOfTheDayWithoutUsername() {
        Match match1 = new Match(this.team1, this.team2, 1, 1, this.now.minusMinutes(5), MatchStatus.SCHEDULED);
        Match match2 = new Match(this.team2, this.team3, 2, 2, this.now.minusMinutes(5), MatchStatus.SCHEDULED);
        this.matchRepository.saveAll(List.of(match1, match2));

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(null, 0, 10);
        List<MatchDTO> result = resultPage.getContent();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).awayTeam().name()).isEqualTo(this.team1.getName());
        assertThat(result.get(1).awayTeam().name()).isEqualTo(this.team2.getName());
    }

    @Test
    @DisplayName("Should update match status to InProgress if match time has passed")
    void testMatchesOfTheDayInProgressUpdate() {
        Match match = new Match(this.team1, this.team2, 0, 0, this.now.minusMinutes(10), MatchStatus.SCHEDULED);
        this.matchRepository.save(match);

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(null, 0, 10);
        MatchDTO result = resultPage.getContent().getFirst();

        assertThat(result.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(this.matchRepository.findById(match.getId()).orElseThrow().getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should update match status to Finished if 3 hours have passed")
    void testMatchesOfTheDayFinishedUpdate() {
        Match match = new Match(this.team1, this.team2, 3, 2, this.now.minusHours(3), MatchStatus.IN_PROGRESS);
        this.matchRepository.save(match);

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(null, 0, 10);
        MatchDTO result = resultPage.getContent().getFirst();

        assertThat(result.status()).isEqualTo(MatchStatus.FINISHED);
        assertThat(this.matchRepository.findById(match.getId()).orElseThrow().getStatus()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("Should return the last 10 matches of a team in descending order")
    void testGetLast10Matches() {
        for (int i = 0; i < 12; i++) {
            Match match = new Match(this.team1, this.team2, i, i + 1, this.now.minusDays(i), MatchStatus.FINISHED);
            this.matchRepository.save(match);
        }

        List<Match> last10 = this.matchService.getLast10Matches(this.team1);

        assertThat(last10).hasSize(10);
        assertThat(last10.get(0).getDate()).isAfter(last10.get(1).getDate());
    }

    @Test
    @DisplayName("Should return home matches of a certain team")
    void testGetHomeMatchesOfATeam() {
        Match homeMatch = new Match(this.team1, this.team3, 1, 0, this.now, MatchStatus.SCHEDULED);
        this.matchRepository.save(homeMatch);

        Page<MatchDTO> result = this.matchService.getHomeMatches(TEST_TEAM3_NAME, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().awayTeam().name()).isEqualTo(this.team1.getName());
    }

    @Test
    @DisplayName("Should return matches of a team between given dates")
    void testGetMatchesOfTeamBetweenDates() {
        LocalDateTime fixedTime = this.now;

        Match matchInRange = new Match(this.team1, this.team2, 5, 3, fixedTime.plusDays(2), MatchStatus.SCHEDULED);
        Match matchOutOfRange = new Match(this.team1, this.team2, 2, 2, fixedTime.plusMonths(2), MatchStatus.SCHEDULED);

        this.matchRepository.saveAll(List.of(matchInRange, matchOutOfRange));

        LocalDate start = fixedTime.toLocalDate();
        LocalDate end = fixedTime.toLocalDate().plusDays(7);

        List<MatchDTO> result = this.matchService.getMatchesOfTeamBetweenDates(this.team1.getName(), start, end);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().homeTeam().name()).isEqualTo(this.team2.getName());
        assertThat(result.getFirst().awayTeam().name()).isEqualTo(this.team1.getName());
    }
}