package com.mlb.mlbportal.services.team;

import java.util.List;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;

public class TeamServiceOperations {

    private TeamServiceOperations() {}
    
    public static void enrichTeamStats(Team team, TeamRepository teamRepository, MatchService matchService) {
        recalculatePct(team);
        calculateGamesBehind(team, teamRepository);
        calculateLast10Games(team, matchService);
        teamRepository.save(team);
    }

    private static void recalculatePct(Team team) {
        int totalGames = team.getWins() + team.getLosses();
        team.setTotalGames(totalGames);
        double pct = totalGames > 0 ? (double) team.getWins() / totalGames : 0.0;
        String formatted = String.format("%.3f", pct).replace("0.", ".");
        team.setPct(formatted.startsWith(".") ? formatted : "." + formatted);
    }

    private static void calculateGamesBehind(Team team, TeamRepository teamRepository) {
        List<Team> divisionTeams = teamRepository.findByLeagueAndDivision(team.getLeague(), team.getDivision());

        divisionTeams.forEach(TeamServiceOperations::recalculatePct);

        divisionTeams.sort((a, b) -> {
            Double pct1 = parseSafePct(a.getPct());
            Double pct2 = parseSafePct(b.getPct());
            return Double.compare(pct2, pct1);
        });

        if (divisionTeams.isEmpty())
            return;

        Team leader = divisionTeams.getFirst();
        double gamesBehind = ((leader.getWins() - team.getWins()) + (team.getLosses() - leader.getLosses())) / 2.0;
        team.setGamesBehind(String.valueOf(gamesBehind));
    }

    private static void calculateLast10Games(Team team, MatchService matchService) {
        List<Match> last10Matches = matchService.getLast10Matches(team);
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
            matchesRecord = numberOfWins + "-" + (last10Matches.size() - numberOfWins);
        }
        team.setLastTen(matchesRecord);
    }

    public static double parseSafePct(String pct) {
        if (pct == null || pct.isEmpty() || pct.equals("-")) return 0.0;
        try {
            String normalized = pct.startsWith(".") ? "0" + pct : pct;
            return Double.parseDouble(normalized);
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }
}