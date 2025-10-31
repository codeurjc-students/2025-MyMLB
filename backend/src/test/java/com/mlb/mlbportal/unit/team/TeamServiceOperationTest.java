package com.mlb.mlbportal.unit.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.team.TeamServiceOperations;
import com.mlb.mlbportal.utils.BuildMocksFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceOperationsTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MatchService matchService;

    private List<Team> teams;
    private Team team;
    private Team leader;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.teams = BuildMocksFactory.setUpTeamMocks();
        this.team = this.teams.get(0);
        this.leader = this.teams.get(1);
    }

    @Test
    @DisplayName("Should enrich team stats using BuildMocksFactory teams")
    void testEnrichTeamStatsWithMocks() {
        when(this.teamRepository.findByLeagueAndDivision(this.team.getLeague(), this.team.getDivision()))
            .thenReturn(Arrays.asList(this.leader, this.team));

        Match win = new Match(this.team, this.leader, 6, 2, null, null);
        Match loss = new Match(this.leader, this.team, 5, 3, null, null);
        when(this.matchService.getLast10Matches(this.team)).thenReturn(List.of(win, loss));

        TeamServiceOperations.enrichTeamStats(this.team, this.teamRepository, this.matchService);

        assertThat(this.team.getTotalGames()).isEqualTo(this.team.getWins() + this.team.getLosses());
        assertThat(this.team.getPct()).isGreaterThan(0.0);
        assertThat(this.team.getGamesBehind()).isGreaterThan(0.0);
        assertThat(this.team.getLastTen()).isEqualTo("1 - 1");

        verify(this.teamRepository).save(this.team);
    }

    @Test
    @DisplayName("Should handle empty last 10 matches")
    void testLastTenEmpty() {
        when(this.teamRepository.findByLeagueAndDivision(League.AL, Division.EAST))
            .thenReturn(Arrays.asList(this.team));
        when(this.matchService.getLast10Matches(this.team)).thenReturn(List.of());

        TeamServiceOperations.enrichTeamStats(this.team, this.teamRepository, this.matchService);

        assertThat(this.team.getLastTen()).isEqualTo("0-0");
    }

    @Test
    @DisplayName("Should handle zero total games")
    void testZeroGames() {
        this.team.setWins(0);
        this.team.setLosses(0);

        when(this.teamRepository.findByLeagueAndDivision(League.AL, Division.EAST))
            .thenReturn(Arrays.asList(this.team));
        when(this.matchService.getLast10Matches(this.team)).thenReturn(Collections.emptyList());

        TeamServiceOperations.enrichTeamStats(this.team, this.teamRepository, this.matchService);

        assertThat(this.team.getTotalGames()).isZero();
        assertThat(this.team.getPct()).isEqualTo(0.0);
    }
}