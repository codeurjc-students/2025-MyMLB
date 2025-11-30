package com.mlb.mlbportal.unit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.mappers.player.PitcherMapper;
import com.mlb.mlbportal.mappers.player.PositionPlayerMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.SearchService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PositionPlayerRepository positionPlayerRepository;

    @Mock
    private PitcherRepository pitcherRepository;

    @Mock
    private StadiumMapper stadiumMapper;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private PositionPlayerMapper positionPlayerMapper;

    @Mock
    private PitcherMapper pitcherMapper;

    @InjectMocks
    private SearchService searchService;

    private List<Stadium> stadiums;
    private List<StadiumInitDTO> stadiumsDtos;

    private List<Team> teams;
    private List<TeamInfoDTO> teamDtos;

    private List<PositionPlayer> positionPlayers;
    private List<PositionPlayerDTO> positionPlayerDtos;

    private List<Pitcher> pitchers;
    private List<PitcherDTO> pitcherDtos;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.stadiums = BuildMocksFactory.setUpStadiums();
        this.stadiumsDtos = BuildMocksFactory.buildStadiumInitDTOMocks();

        this.teams = BuildMocksFactory.setUpTeamMocks();
        this.teamDtos = BuildMocksFactory.buildTeamInfoDTOMocks(this.teams);

        this.positionPlayers = BuildMocksFactory.buildPositionPlayers(this.teams);
        this.positionPlayerDtos = BuildMocksFactory.buildPositionPlayerDTOs();

        this.pitchers = BuildMocksFactory.buildPitchers(this.teams);
        this.pitcherDtos = BuildMocksFactory.buildPitcherDTOs();
    }

    @Test
    @DisplayName("Should return paginated stadiums that match the input search")
    void testSearchStadium() {
        when(this.stadiumRepository.findByNameContainingIgnoreCase("St")).thenReturn(this.stadiums);
        when(this.stadiumMapper.toStadiumInitDTO(this.stadiums.getFirst())).thenReturn(this.stadiumsDtos.getFirst());
        when(this.stadiumMapper.toStadiumInitDTO(this.stadiums.get(1))).thenReturn(this.stadiumsDtos.get(1));
        when(this.stadiumMapper.toStadiumInitDTO(this.stadiums.get(2))).thenReturn(this.stadiumsDtos.get(2));

        Page<StadiumInitDTO> result = this.searchService.searchStadiums("St", 0, 10);

        assertThat(result.getContent()).hasSize(3).containsExactlyElementsOf(this.stadiumsDtos);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return paginated teams that match the input search")
    void testSearchTeam() {
        when(this.teamRepository.findByNameContainingIgnoreCase("Te")).thenReturn(this.teams);
        when(this.teamMapper.toTeamInfoDTO(this.teams.getFirst())).thenReturn(this.teamDtos.getFirst());
        when(this.teamMapper.toTeamInfoDTO(this.teams.get(1))).thenReturn(this.teamDtos.get(1));
        when(this.teamMapper.toTeamInfoDTO(this.teams.get(2))).thenReturn(this.teamDtos.get(2));

        Page<TeamInfoDTO> result = this.searchService.searchTeams("Te", 0, 10);

        assertThat(result.getContent()).hasSize(3).containsExactlyElementsOf(this.teamDtos);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return paginated position players that match the input search")
    void testSearchPositionPlayer() {
        when(this.positionPlayerRepository.findByNameContainingIgnoreCase("pl")).thenReturn(this.positionPlayers);
        when(this.positionPlayerMapper.toPositionPlayerDTO(this.positionPlayers.getFirst())).thenReturn(this.positionPlayerDtos.getFirst());
        when(this.positionPlayerMapper.toPositionPlayerDTO(this.positionPlayers.get(1))).thenReturn(this.positionPlayerDtos.get(1));

        Page<PositionPlayerDTO> result = this.searchService.searchPositionPlayers("pl", 0, 10);

        assertThat(result.getContent()).hasSize(2).containsExactlyElementsOf(this.positionPlayerDtos);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return paginated pitchers that match the input search")
    void testSearchPitcher() {
        when(this.pitcherRepository.findByNameContainingIgnoreCase("pl")).thenReturn(this.pitchers);
        when(this.pitcherMapper.toPitcherDTO(this.pitchers.getFirst())).thenReturn(this.pitcherDtos.get(0));

        Page<PitcherDTO> result = this.searchService.searchPitchers("pl", 0, 10);

        assertThat(result.getContent()).hasSize(1).containsExactlyElementsOf(this.pitcherDtos);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}