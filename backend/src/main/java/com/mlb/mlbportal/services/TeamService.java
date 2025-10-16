package com.mlb.mlbportal.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;

@Service
public class TeamService {
    private static final List<League> LEAGUE_ORDER = List.of(League.AL, League.NL);
    private static final List<Division> DIVISION_ORDER = List.of(Division.EAST, Division.CENTRAL, Division.WEST);

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    public List<TeamDTO> getTeams() {
        List<Team> teams = teamRepository.findAll();
        teams.forEach(this::enrichTeamStats);
        return teamMapper.toTeamDTOList(teams);
    }

    public Map<League, Map<Division, List<TeamDTO>>> getStandings() {
        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(this::enrichTeamStats);

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
                        .collect(Collectors.toList());

                divisionMap.put(division, sorted);
            }

            standings.put(league, divisionMap);
        }
        return standings;
    }

    private void enrichTeamStats(Team team) {
        recalculatePct(team);
        calculateGamesBehind(team);
    }

    private void recalculatePct(Team team) {
        int totalGames = team.getWins() + team.getLosses();
        team.setTotalGames(totalGames);
        double pct = totalGames > 0 ? (double) team.getWins() / totalGames : 0.0;
        double truncatedPct = ((int) (pct * 1000)) / 1000.0;
        team.setPct(truncatedPct);
    }

    private void calculateGamesBehind(Team team) {
        List<Team> divisionTeams = teamRepository.findByLeagueAndDivision(team.getLeague(), team.getDivision());

        divisionTeams.forEach(this::recalculatePct);

        divisionTeams.sort((a, b) -> Double.compare(b.getPct(), a.getPct()));

        if (divisionTeams.isEmpty())
            return;

        Team leader = divisionTeams.get(0);
        double gamesBehind = ((leader.getWins() - team.getWins()) + (team.getLosses() - leader.getLosses())) / 2.0;
        team.setGamesBehind(gamesBehind);
    }
}