package com.mlb.mlbportal.services.team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.UserService;
import com.mlb.mlbportal.services.player.PlayerService;

import jakarta.transaction.Transactional;
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
    private final UserService userService;

    public List<TeamInfoDTO> getTeams() {
        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(team -> TeamServiceOperations.enrichTeamStats(team, teamRepository, matchService));
        return this.teamMapper.toTeamInfoDTOList(teams);
    }

    public Team getTeam(String teamName) {
        return this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
    }

    @Transactional
    public TeamInfoDTO getTeamInfo(String teamName) {
        Team team = this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
        TeamServiceOperations.enrichTeamStats(team, teamRepository, matchService);
        
        List<PositionPlayer> positionPlayers = this.playerService.getUpdatedPositionPlayersOfTeam(team);
        List<Pitcher> pitchers = this.playerService.getUpdatedPitchersOfTeam(team);

        positionPlayers.forEach(p -> p.setTeam(team));
        pitchers.forEach(p -> p.setTeam(team));

        team.setPositionPlayers(positionPlayers);
        team.setPitchers(pitchers);
        this.teamRepository.save(team);
        return this.teamMapper.toTeamInfoDTO(team);
    }

    private List<Map.Entry<League, Division>> getPrioritizedLeagueDivisionOrder(UserEntity user) {
        Set<Map.Entry<League, Division>> favPairs = user.getFavTeams().stream()
                .map(team -> Map.entry(team.getLeague(), team.getDivision()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Map.Entry<League, Division>> ordered = new ArrayList<>(favPairs);

        for (League league : LEAGUE_ORDER) {
            for (Division division : DIVISION_ORDER) {
                Map.Entry<League, Division> pair = Map.entry(league, division);
                if (!ordered.contains(pair)) {
                    ordered.add(pair);
                }
            }
        }
        return ordered;
    }

    public Map<League, Map<Division, List<TeamDTO>>> getStandings(String username) {
        UserEntity user = null;
        if (username != null && !username.isBlank()) {
            user = this.userService.getUser(username);
        }

        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(team -> TeamServiceOperations.enrichTeamStats(team, teamRepository, matchService));

        Map<League, Map<Division, List<Team>>> grouped = teams.stream()
                .collect(Collectors.groupingBy(Team::getLeague, Collectors.groupingBy(Team::getDivision)));

        Map<League, Map<Division, List<TeamDTO>>> standings = new LinkedHashMap<>();

        List<Map.Entry<League, Division>> leagueDivisionOrder = (user != null)
                ? getPrioritizedLeagueDivisionOrder(user)
                : LEAGUE_ORDER.stream()
                .flatMap(league -> DIVISION_ORDER.stream().map(div -> Map.entry(league, div)))
                .toList();

        for (Map.Entry<League, Division> entry : leagueDivisionOrder) {
            League league = entry.getKey();
            Division division = entry.getValue();

            List<Team> divisionTeams = grouped.getOrDefault(league, Map.of())
                    .getOrDefault(division, List.of());

            List<TeamDTO> sorted = divisionTeams.stream()
                    .sorted((a, b) -> Double.compare(b.getPct(), a.getPct()))
                    .map(this.teamMapper::toTeamDTO)
                    .toList();

            standings.computeIfAbsent(league, l -> new LinkedHashMap<>())
                    .put(division, sorted);
        }
        return standings;
    }
}