package com.mlb.mlbportal.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TeamService {
    private static final List<League> LEAGUE_ORDER = List.of(League.AL, League.NL);
    private static final List<Division> DIVISION_ORDER = List.of(Division.EAST, Division.CENTRAL, Division.WEST);

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final MatchService matchService;

    public List<TeamInfoDTO> getTeams() {
        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(this::enrichTeamStats);
        return this.teamMapper.toTeamInfoDTOList(teams);
    }

    public Team getTeam(String teamName) {
        return this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
    }

    public TeamInfoDTO getTeamInfo(String teamName) {
        Team team = this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
        this.enrichTeamStats(team);
        return this.teamMapper.toTeamInfoDTO(team);
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
                        .toList();

                divisionMap.put(division, sorted);
            }

            standings.put(league, divisionMap);
        }
        return standings;
    }

    private void enrichTeamStats(Team team) {
        this.recalculatePct(team);
        this.calculateGamesBehind(team);
        this.calculateLast10Games(team);
        this.teamRepository.save(team);
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

    private void calculateLast10Games(Team team) {
        List<Match> last10Matches = this.matchService.getLast10Matches(team);
        String matchesRecord;
        if (last10Matches.isEmpty()) {
            matchesRecord = "0-0";
        }
        else {
            int numberOfWins = 0;
            for (Match match : last10Matches) {
                boolean isHomeTeam = team.equals(match.getHomeTeam());
                int teamScore = isHomeTeam ? match.getHomeScore() : match.getAwayScore();
                int awayScore = isHomeTeam ? match.getAwayScore() : match.getHomeScore();

                if (teamScore > awayScore) {
                    numberOfWins++;
                }
            }
            matchesRecord = numberOfWins + " - " + (last10Matches.size() - numberOfWins);
        }
        team.setLastTen(matchesRecord);
    }
}