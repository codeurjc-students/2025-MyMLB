package com.mlb.mlbportal.services.player;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    public static void setQueryParams(Query dataQuery, Query countQuery, List<String> teamNames, League league, Division division, boolean hasTeamFilter, boolean hasLeagueFilter, boolean hasDivisionFilter) {
        if (hasTeamFilter) {
            dataQuery.setParameter("teamNames", teamNames);
            countQuery.setParameter("teamNames", teamNames);
        }
        if (hasLeagueFilter) {
            dataQuery.setParameter("league", league);
            countQuery.setParameter("league", league);
        }
        if (hasDivisionFilter) {
            dataQuery.setParameter("division", division);
            countQuery.setParameter("division", division);
        }
    }

    public static String buildDataQuery(String stat, String tableName, boolean hasTeam, boolean hasLeague, boolean hasDiv) {
        List<String> reversed = List.of("era", "whip");
        String order = (reversed.contains(stat.toLowerCase())) ? "ASC" : "DESC";

        return "SELECT new com.mlb.mlbportal.dto.player.PlayerRankingsDTO(p.name, p.picture.url, p." + stat + ") " +
                buildCommonQueryBody(stat, tableName, hasTeam, hasLeague, hasDiv) +
                "ORDER BY p." + stat + " " + order;
    }

    public static String buildCountQuery(String stat, String tableName, boolean hasTeam, boolean hasLeague, boolean hasDiv) {
        return "SELECT count(p.id) " +
                buildCommonQueryBody(stat, tableName, hasTeam, hasLeague, hasDiv);
    }

    /**
     * Prepares and build de sql query to obtain the rankings.
     * Wraps the common logic fo both data and count queries.
     *
     * @param stat the given stat.
     * @param tableName source table of the query.
     * @param hasTeamFilter states if the team filter has been selected.
     * @param hasLeagueFilter states if the league filter has been selected.
     * @param hasDivisionFilter states if the division filter has been selected.
     * @return the query.
     */
    private static String buildCommonQueryBody(String stat, String tableName, boolean hasTeamFilter, boolean hasLeagueFilter, boolean hasDivisionFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append("FROM ").append(tableName).append(" p ");
        sb.append("JOIN p.team t ");

        List<String> conditions = new ArrayList<>();
        boolean isPitcher = "Pitcher".equals(tableName);

        buildQualifiedConditions(conditions, isPitcher, stat);
        buildFilterConditions(conditions, hasTeamFilter, hasLeagueFilter, hasDivisionFilter);

        if (!conditions.isEmpty()) {
            sb.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }
        return sb.toString();
    }

    /**
     * Build the conditions that made a player qualified to be on the ranking.
     * For a PositionPlayer to be qualified the player must have a minimum of 3.1 plate appearances (At Bats + Walks) per game of the team.
     * For a Pitcher to be qualified the pitcher must have a minimum of 1 inningPitched per game of the team.
     * Since this conditions will fail at the beginning of the season, minimum values are established.
     *
     * @param conditions list of the query conditions.
     * @param isPitcher boolean value that states whether the player is a pitcher or not.
     * @param stat the filtered stat.
     */
    private static void buildQualifiedConditions(List<String> conditions, boolean isPitcher, String stat) {
        if (!isPitcher) {
            // MLB Rule (only for average, obp and slugging)"
            if ("average".equalsIgnoreCase(stat) || "obp".equalsIgnoreCase(stat) || "slugging".equalsIgnoreCase(stat)) {
                conditions.add("(1.0 * p.atBats + 1.0 * p.walks) >= (t.totalGames * 3.1)");
            }
            else {
                // Other stats like homeRuns, rbis, etc.
                conditions.add("(p.atBats + p.walks) >= 1");
            }
        }
        else {
            // Conditions for pitchers
            if ("era".equalsIgnoreCase(stat) || "whip".equalsIgnoreCase(stat) || "k9".equalsIgnoreCase(stat)) {
                conditions.add("p.inningsPitched >= (t.totalGames * 1.0)");
            }
            else if ("saves".equalsIgnoreCase(stat) || "saveOpportunities".equalsIgnoreCase(stat)) {
                // Only for reliever pitchers
                conditions.add("p.games >= (t.totalGames / 3.0)");
            }
            else {
                // Other stats like wins, losses, etc.
                conditions.add("p.games >= 1");
            }
        }
    }

    /**
     * Build the conditions of the query based on the filters applied by the user.
     *
     * @param conditions list of the query conditions.
     * @param hasTeamFilter states if the team filter has been selected.
     * @param hasLeagueFilter  states if the league filter has been selected.
     * @param hasDivisionFilter  states if the division filter has been selected.
     */
    private static void buildFilterConditions(List<String> conditions, boolean hasTeamFilter, boolean hasLeagueFilter, boolean hasDivisionFilter) {
        if (hasTeamFilter) {
            conditions.add("t.name IN :teamNames");
        }
        if (hasLeagueFilter) {
            conditions.add("t.league = :league");
        }
        if (hasDivisionFilter) {
            conditions.add("t.division = :division");
        }
    }

    /**
     * Verify if the provided stat matches any of those stored in the PositionPlayer or Pitcher tables.
     *
     * @param statName the given stat.
     * @param playerType position player or pitcher.
     * @return true if matches, false otherwise
     */
    public static boolean isValidStat(String statName, String playerType) {
        Class<?> className = ("position".equals(playerType)) ? PositionPlayer.class : Pitcher.class;
        try {
            className.getDeclaredField(statName);
            return true;
        }
        catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * Obtains the source table of the query, depending on the player type.
     *
     * @param playerType position player or pitcher.
     * @return the name of the table.
     */
    public static String getTableName(String playerType) {
        if ("position".equals(playerType)) {
            return "PositionPlayer";
        }
        else if ("pitcher".equals(playerType)) {
            return "Pitcher";
        }
        throw new IllegalArgumentException("The provided player type is not valid");
    }

    public static List<String> getPlayerStats(String playerType) {
        if ("position".equals(playerType)) {
            return List.of(
                    "atBats",
                    "hits",
                    "walks",
                    "homeRuns",
                    "rbis",
                    "average",
                    "obp",
                    "ops",
                    "doubles",
                    "triples",
                    "slugging"
            );
        }
        return List.of(
                "games",
                "era",
                "wins",
                "losses",
                "inningsPitched",
                "totalStrikeouts",
                "walks",
                "hitsAllowed",
                "runsAllowed",
                "saves",
                "saveOpportunities",
                "whip"
        );
    }
}