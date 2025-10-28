package com.mlb.mlbportal.integration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.mappers.MatchMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchServiceIntegrationTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    @SuppressWarnings("unused")
    private MatchMapper matchMapper;

    private Team team1, team2;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();

        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL, Division.EAST);
        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.AL, Division.EAST);

        this.team1 = this.teamRepository.save(this.team1);
        this.team2 = this.teamRepository.save(this.team2);
    }

    @Test
    @DisplayName("Should update match status to InProgress if match time has passed")
    void testMatchesOfTheDayInProgressUpdate() {
        Match match = new Match(this.team1, this.team2, 0, 0, LocalDateTime.now().minusMinutes(10), MatchStatus.SCHEDULED);
        this.matchRepository.save(match);

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(0, 10);
        List<MatchDTO> result = resultPage.getContent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(MatchStatus.IN_PROGRESS);

        Match updated = this.matchRepository.findById(match.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should update match status to Finished if 3 hours have passed")
    void testMatchesOfTheDayFinishedUpdate() {
        Match match = new Match(this.team1, this.team2, 3, 2, LocalDateTime.now().minusHours(3), MatchStatus.IN_PROGRESS);
        this.matchRepository.save(match);

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(0, 10);
        List<MatchDTO> result = resultPage.getContent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(MatchStatus.FINISHED);

        Match updated = this.matchRepository.findById(match.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("Should return the last 10 matches of a team in descending order")
    void testGetLast10MatchesIntegration() {
        for (int i = 0; i < 12; i++) {
            Match match = new Match(this.team1, this.team2, i, i + 1, LocalDateTime.now().minusDays(i), MatchStatus.FINISHED);
            this.matchRepository.save(match);
        }

        List<Match> last10 = this.matchService.getLast10Matches(this.team1);

        assertThat(last10).hasSize(10);
        assertThat(last10.get(0).getDate()).isAfter(last10.get(1).getDate());
    }
}