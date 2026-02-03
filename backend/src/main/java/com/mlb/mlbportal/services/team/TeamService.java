package com.mlb.mlbportal.services.team;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.dto.team.UpdateTeamRequest;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.UserService;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;

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
    private final PaginationHandlerService paginationHandlerService;
    private final StadiumRepository stadiumRepository;

    @Transactional(readOnly = true)
    public Page<TeamInfoDTO> getTeams(int page, int size) {
        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(team -> TeamServiceOperations.enrichTeamStats(team, teamRepository, matchService));
        return this.paginationHandlerService.paginateAndMap(teams, page, size, this.teamMapper::toTeamInfoDTO);
    }

    @Transactional(readOnly = true)
    public Page<TeamSummary> getAvailableTeams(int page, int size) {
        List<Team> teams = this.teamRepository.findAvailableTeams();
        teams.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));
        return this.paginationHandlerService.paginateAndMap(teams, page, size, this.teamMapper::toTeamSummaryDTO);
    }

    @Transactional(readOnly = true)
    public Team getTeam(String teamName) {
        return this.teamRepository.findByNameOrThrow(teamName);
    }

    @Transactional
    public TeamInfoDTO getTeamInfo(String teamName) {
        Team team = this.teamRepository.findByNameOrThrow(teamName);
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

    @Transactional(readOnly = true)
    public Map<League, Map<Division, List<TeamDTO>>> getStandings(String username) {
        UserEntity user = null;
        if (username != null && !username.isBlank()) {
            user = this.userService.getUser(username);
        }

        List<Team> teams = this.teamRepository.findAll();

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

    @Transactional
    public void updateRanking(Team awayTeam, Team homeTeam) {
        TeamServiceOperations.enrichTeamStats(awayTeam, this.teamRepository, this.matchService);
        TeamServiceOperations.enrichTeamStats(homeTeam, this.teamRepository, this.matchService);
        this.teamRepository.save(awayTeam);
        this.teamRepository.save(homeTeam);
    }

    @Transactional
    public void updateTeam(String teamName, UpdateTeamRequest request) {
        Team team = this.teamRepository.findByNameOrThrow(teamName);

        request.city().ifPresent(newCity -> {
            String updatedName = team.getName().replace(team.getCity(), newCity);
            team.setCity(newCity);
            team.setName(updatedName);
        });
        request.newChampionship().ifPresent(team.getChampionships()::addLast);
        request.newInfo().ifPresent(team::setGeneralInfo);
        request.newStadiumName().ifPresent(stadiumName -> {
            Stadium stadium = this.stadiumRepository.findByNameOrThrow(stadiumName);
            if (stadium.getTeam() != null) {
                throw new IllegalArgumentException("The stadium " + stadium.getName() + " already has a team");
            }
            team.setStadium(stadium);
        });
        this.teamRepository.save(team);
    }
}