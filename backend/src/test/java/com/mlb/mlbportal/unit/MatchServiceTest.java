package com.mlb.mlbportal.unit;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.mappers.MatchMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.models.interfaces.TimeProvider;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.services.MatchService;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_WINS;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {
    @Mock
    private MatchRepository matchRepository;
    
    @Mock
    private MatchMapper matchMapper;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private MatchService matchService;

    private Match match1, match2, match3;
    private Team team1, team2, team3;

    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 10, 23, 15, 0);

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL, Division.EAST);
        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.AL, Division.WEST);
        this.team3 = new Team(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.NL, Division.EAST);

        this.match1 = new Match(team1, team2, 0, 0, fixedNow.minusMinutes(5), MatchStatus.Scheduled);
        this.match2 = new Match(team2, team3, 4, 9, fixedNow.minusMinutes(4), MatchStatus.InProgress);
        this.match3 = new Match(team3, team1, 10, 14, fixedNow.minusMinutes(3), MatchStatus.Finished);
    }

    private List<MatchDTO> buildMockDTOs() {
        TeamSummary teamDTO1 = new TeamSummary(this.team1.getName(), this.team1.getAbbreviation(), this.team1.getLeague(), this.team1.getDivision());
        TeamSummary teamDTO2 = new TeamSummary(this.team2.getName(), this.team2.getAbbreviation(), this.team2.getLeague(), this.team2.getDivision());
        TeamSummary teamDTO3 = new TeamSummary(this.team3.getName(), this.team3.getAbbreviation(), this.team3.getLeague(), this.team3.getDivision());

        MatchDTO dto1 = new MatchDTO(teamDTO1, teamDTO2, this.match1.getHomeScore(), this.match1.getAwayScore(), match1.getDate(), match1.getStatus());
        MatchDTO dto2 = new MatchDTO(teamDTO2, teamDTO3, this.match2.getHomeScore(), this.match2.getAwayScore(), match2.getDate(), match2.getStatus());
        MatchDTO dto3 = new MatchDTO(teamDTO3, teamDTO1, this.match3.getHomeScore(), this.match3.getAwayScore(), match3.getDate(), match3.getStatus());

        return Arrays.asList(dto1, dto2, dto3);
    }

    @Test
    @DisplayName("Should return all the matches scheduled for today")
    void testGetMatchesOfTheDay() {
        when(this.timeProvider.today()).thenReturn(this.fixedNow.toLocalDate());
        when(this.timeProvider.now()).thenReturn(this.fixedNow);

        LocalDateTime startOfDay = this.fixedNow.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = this.fixedNow.toLocalDate().atTime(LocalTime.MAX);

        List<Match> mockMatches = Arrays.asList(match1, match2, match3);
        List<MatchDTO> expectedResult = this.buildMockDTOs();

        when(this.matchRepository.findByDateBetween(startOfDay, endOfDay)).thenReturn(mockMatches);
        when(this.matchMapper.toMatchDTO(any(Match.class))).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            return expectedResult.stream()
                    .filter(dto -> dto.date().equals(m.getDate()))
                    .findFirst()
                    .orElseThrow();
        });

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(0, 10);
        List<MatchDTO> result = resultPage.getContent();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(expectedResult);
    }

    @Test
    @DisplayName("Should return the last 10 matches of a team. If the team has played less than 10 games, return those matches")
    void testGetLast10Matches() {
        List<Match> mockMatches = Arrays.asList(match1, match2, match3);
        when(this.matchRepository.findTop10ByHomeTeamOrAwayTeamOrderByDateDesc(team1, team1)).thenReturn(mockMatches);

        List<Match> result = this.matchService.getLast10Matches(team1);

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(match1, match2, match3);
    }
}