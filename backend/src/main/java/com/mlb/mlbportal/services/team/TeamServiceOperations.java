package com.mlb.mlbportal.services.team;

import java.util.List;
import java.util.Locale;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.TeamRepository;

public class TeamServiceOperations {

    private TeamServiceOperations() {}
    
    public static void enrichTeamStats(Team team, TeamRepository teamRepository) {
        recalculatePct(team);
        calculateGamesBehind(team, teamRepository);
        teamRepository.save(team);
    }

    private static void recalculatePct(Team team) {
        int totalGames = team.getWins() + team.getLosses();
        team.setTotalGames(totalGames);
        double pct = totalGames > 0 ? (double) team.getWins() / totalGames : 0.0;
        String formatted = String.format(Locale.US, "%.3f", pct);
        if (formatted.startsWith("0.")) {
            team.setPct(formatted.substring(1));
        }
        else {
            team.setPct(formatted);
        }
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
        team.setGamesBehind(gamesBehind);
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