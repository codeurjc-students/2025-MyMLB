package com.mlb.mlbportal.unit;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;

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
    private UserService userService;

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
    @DisplayName("Should return today's matches sorted normally when no user is provided")
    void testGetMatchesOfTheDayWithoutUser() {
        this.mockClockAndRepository();
        this.mockMatchMapper();

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(null, 0, 10);
        List<MatchDTO> result = resultPage.getContent();

        assertThat(result).hasSize(this.matches.size()).containsExactlyElementsOf(this.mockMatchDTOs);

        verify(this.matchRepository).saveAll(this.matches);
        verify(this.userService, never()).getUser(any());
    }

    @Test
    @DisplayName("Should prioritize matches with user's favorite teams at the top")
    void testGetMatchesOfTheDayWithUserFavorites() {
        this.mockClockAndRepository();
        this.mockMatchMapper();

        UserEntity mockUser = BuildMocksFactory.setUpUsers().getLast();
        mockUser.setFavTeams(Set.of(this.teams.getFirst()));
        when(this.userService.getUser(TEST_USER_USERNAME)).thenReturn(mockUser);

        Page<MatchDTO> resultPage = this.matchService.getMatchesOfTheDay(TEST_USER_USERNAME, 0, 10);
        List<MatchDTO> result = resultPage.getContent();

        Team fav = this.teams.getFirst();
        boolean involvesFav = this.matches.stream()
                .filter(m -> m.getDate().equals(result.getFirst().date()))
                .anyMatch(m -> m.getHomeTeam().equals(fav) || m.getAwayTeam().equals(fav));

        assertThat(result).hasSize(this.matches.size());
        assertThat(involvesFav).isTrue();

        verify(this.userService).getUser(TEST_USER_USERNAME);
        verify(this.matchRepository).saveAll(this.matches);
    }

    private void mockClockAndRepository() {
        when(this.clock.instant()).thenReturn(this.fixedNow.atZone(ZoneId.of("Europe/Madrid")).toInstant());
        when(this.clock.getZone()).thenReturn(ZoneId.of("Europe/Madrid"));

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        when(this.matchRepository.findByDateBetween(startOfDay, endOfDay)).thenReturn(this.matches);
    }

    private void mockMatchMapper() {
        when(this.matchMapper.toMatchDTO(any(Match.class))).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            return this.mockMatchDTOs.stream()
                    .filter(dto -> dto.date().equals(m.getDate()))
                    .findFirst()
                    .orElseThrow();
        });
    }

    @Test
    @DisplayName("Should return the last 10 matches of a team")
    void testGetLast10Matches() {
        Team team1 = teams.getFirst();

        when(this.matchRepository.findTop10ByHomeTeamOrAwayTeamOrderByDateDesc(team1, team1))
                .thenReturn(matches);

        List<Match> result = this.matchService.getLast10Matches(team1);

        assertThat(result).hasSize(matches.size()).containsExactlyElementsOf(matches);
    }
}