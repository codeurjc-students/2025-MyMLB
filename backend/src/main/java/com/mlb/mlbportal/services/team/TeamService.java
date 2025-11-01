package com.mlb.mlbportal.services.team;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.player.PlayerService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TeamService {
    private static final List<League> LEAGUE_ORDER = List.of(League.AL, League.NL);
    private static final List<Division> DIVISION_ORDER = List.of(Division.EAST, Division.CENTRAL, Division.WEST);

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final MatchService matchService;
    private final PlayerService playerService;

    public List<TeamInfoDTO> getTeams() {
        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(team -> TeamServiceOperations.enrichTeamStats(team, teamRepository, matchService));
        return this.teamMapper.toTeamInfoDTOList(teams);
    }

    public Team getTeam(String teamName) {
        return this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
    }

    public TeamInfoDTO getTeamInfo(String teamName) {
        Team team = this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
        TeamServiceOperations.enrichTeamStats(team, teamRepository, matchService);
        
        List<PositionPlayer> positionPlayers = this.playerService.getUpdatedPositionPlayersOfTeam(teamName);
        List<Pitcher> pitchers = this.playerService.getUpdatedPitchersOfTeam(teamName);

        positionPlayers.forEach(p -> p.setTeam(team));
        pitchers.forEach(p -> p.setTeam(team));

        team.setPositionPlayers(positionPlayers);
        team.setPitchers(pitchers);
        this.teamRepository.save(team);
        return this.teamMapper.toTeamInfoDTO(team);
    }

    public Map<League, Map<Division, List<TeamDTO>>> getStandings() {
        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(team -> TeamServiceOperations.enrichTeamStats(team, teamRepository, matchService));

        Map<League, Map<Division, List<Team>>> grouped = teams.stream()
                .collect(Collectors.groupingBy(Team::getLeague, Collectors.groupingBy(Team::getDivision)));

        Map<League, Map<Division, List<TeamDTO>>> standings = new LinkedHashMap<>();

        for (League league : LEAGUE_ORDER) {
            Map<Division, List<TeamDTO>> divisionMap = new LinkedHashMap<>();

            for (Division division : DIVISION_ORDER) {
                List<Team> divisionTeams = grouped.getOrDefault(league, Map.of())
                        .getOrDefault(division, List.of());

                List<TeamDTO> sorted = divisionTeams.stream()
                        .sorted((a, b) -> Double.compare(b.getPct(), a.getPct()))
                        .map(this.teamMapper::toTeamDTO)
                        .toList();

                divisionMap.put(division, sorted);
            }

            standings.put(league, divisionMap);
        }
        return standings;
    }
}