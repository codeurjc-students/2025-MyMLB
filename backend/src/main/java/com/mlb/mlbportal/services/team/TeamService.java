package com.mlb.mlbportal.services.team;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mlb.mlbportal.dto.team.HistoricRankingDTO;
import com.mlb.mlbportal.dto.team.RunsStatsDTO;
import com.mlb.mlbportal.dto.team.WinDistributionDTO;
import com.mlb.mlbportal.dto.team.WinsPerRivalDTO;
import com.mlb.mlbportal.handler.badRequest.InvalidTypeException;
import com.mlb.mlbportal.mappers.DailyStandingsMapper;
import com.mlb.mlbportal.models.DailyStandings;
import com.mlb.mlbportal.repositories.DailyStandingsRepository;
import com.mlb.mlbportal.repositories.MatchRepository;
import org.springframework.cache.annotation.Cacheable;
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
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.UserService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TeamService {
    private static final List<League> LEAGUE_ORDER = List.of(League.AL, League.NL);
    private static final List<Division> DIVISION_ORDER = List.of(Division.EAST, Division.CENTRAL, Division.WEST);

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final UserService userService;
    private final PaginationHandlerService paginationHandlerService;
    private final StadiumRepository stadiumRepository;
    private final MatchRepository matchRepository;
    private final DailyStandingsRepository dailyStandingsRepository;
    private final DailyStandingsMapper dailyStandingsMapper;

    @Transactional(readOnly = true)
    public Page<TeamInfoDTO> getTeams(int page, int size) {
        List<Team> teams = this.teamRepository.findAll();
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

    @Transactional(readOnly = true)
    public TeamInfoDTO getTeamInfo(String teamName) {
        Team team = this.teamRepository.findByNameOrThrow(teamName);
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
                    .sorted((t1, t2) -> {
                        Double pct1 = TeamServiceOperations.parseSafePct(t1.getPct());
                        Double pct2 = TeamServiceOperations.parseSafePct(t2.getPct());
                        int pctCompare = Double.compare(pct2, pct1);
                        if (pctCompare == 0) {
                            return Integer.compare(t2.getWins(), t1.getWins());
                        }
                        return pctCompare;
                    }).map(this.teamMapper::toTeamDTO).toList();

            standings.computeIfAbsent(league, l -> new LinkedHashMap<>()).put(division, sorted);
        }
        return standings;
    }

    @Transactional(readOnly = true)
    public List<TeamDTO> getRivalTeams(String teamName) {
        this.teamRepository.findByNameOrThrow(teamName);
        List<Team> queryResult = this.teamRepository.findRivals(teamName);
        return this.teamMapper.toTeamDTOList(queryResult);
    }

    @Transactional(readOnly = true)
    public List<WinsPerRivalDTO> getWinsPerRivals(String fixedTeamName, Set<String> rivalTeamsNames) {
        if (rivalTeamsNames == null || rivalTeamsNames.isEmpty()) {
            throw new InvalidTypeException("The rival teams are required");
        }
        if (rivalTeamsNames.contains(fixedTeamName)) {
            throw new InvalidTypeException("The rival team must differ from the current team");
        }
        return this.matchRepository.findWinsPerRival(fixedTeamName, rivalTeamsNames);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "runs-per-rival", key = "#teams")
    public List<RunsStatsDTO> getRunStatsPerRival(Set<String> teams) {
        return this.teamRepository.findRunsStats(teams);
    }

    @Transactional(readOnly = true)
    public WinDistributionDTO getWinDistribution(String teamName) {
        this.teamRepository.findByNameOrThrow(teamName);
        return this.matchRepository.findWinDistribution(teamName);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "historic-ranking", key = "#teams")
    public Map<String, List<HistoricRankingDTO>> getHistoricRanking(Set<String> teams, LocalDate dateFrom) {
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusMonths(1);
        }
        List<DailyStandings> queryResult = this.dailyStandingsRepository.findHistoricRanking(teams, dateFrom);
        return queryResult.stream()
                .map(this.dailyStandingsMapper::toHistoricRankingDTO)
                .collect(Collectors.groupingBy(HistoricRankingDTO::teamName));
    }

    @Transactional
    public void updateRanking(Team awayTeam, Team homeTeam) {
        Set<Team> teamsToUpdate = new HashSet<>();
        teamsToUpdate.addAll(this.teamRepository.findByLeagueAndDivision(awayTeam.getLeague(), awayTeam.getDivision()));
        teamsToUpdate.addAll(this.teamRepository.findByLeagueAndDivision(homeTeam.getLeague(), homeTeam.getDivision()));
        teamsToUpdate.forEach(team -> TeamServiceOperations.enrichTeamStats(team, this.teamRepository));
        this.teamRepository.saveAll(teamsToUpdate);
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