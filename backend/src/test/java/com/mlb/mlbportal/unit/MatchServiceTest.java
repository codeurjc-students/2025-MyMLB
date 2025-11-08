package com.mlb.mlbportal.unit;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
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
import com.mlb.mlbportal.mappers.MatchMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchMapper matchMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private MatchService matchService;

    private List<Team> teams;
    private List<Match> matches;
    private List<MatchDTO> mockMatchDTOs;

    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 10, 23, 15, 0);

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.teams = BuildMocksFactory.setUpTeamMocks();
        this.matches = BuildMocksFactory.setUpMatches(this.teams, fixedNow);
        this.mockMatchDTOs = BuildMocksFactory.buildMatchDTOMocks(this.matches);
    }

    @Test
    @DisplayName("Should return all the matches scheduled for today")
    void testGetMatchesOfTheDay() {
        when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.of("Europe/Madrid")).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/Madrid"));

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        when(this.matchRepository.findByDateBetween(startOfDay, endOfDay)).thenReturn(matches);
        when(this.matchMapper.toMatchDTO(any(Match.class))).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            return mockMatchDTOs.stream()
                    .filter(dto -> dto.date().equals(m.getDate()))
                    .findFirst()
                    .orElseThrow();
        });

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(TEST_USER_USERNAME,0, 10);
        List<MatchDTO> result = resultPage.getContent();

        assertThat(result).hasSize(matches.size()).containsExactlyElementsOf(mockMatchDTOs);
    }

    @Test
    @DisplayName("Should return the last 10 matches of a team")
    void testGetLast10Matches() {
        Team team1 = teams.get(0);

        when(this.matchRepository.findTop10ByHomeTeamOrAwayTeamOrderByDateDesc(team1, team1))
                .thenReturn(matches);

        List<Match> result = this.matchService.getLast10Matches(team1);

        assertThat(result).hasSize(matches.size()).containsExactlyElementsOf(matches);
    }
}