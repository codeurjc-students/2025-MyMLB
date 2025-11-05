package com.mlb.mlbportal.services.player;

import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.Player;
import com.mlb.mlbportal.models.player.PositionPlayer;

public class PlayerServiceOperations {

    public static boolean updatePlayerStats(Player player) {
        return switch (player) {
            case PositionPlayer positionPlayer -> updatePositionPlayerStats(positionPlayer);
            case Pitcher pitcher -> updatePitcherStats(pitcher);
            default -> false;
        };
    }

    private static boolean updatePositionPlayerStats(PositionPlayer player) {
        double oldAvg = player.getAverage();
        double oldObp = player.getObp();
        double oldSlg = player.getSlugging();
        double oldOps = player.getOps();

        calculateAverage(player);
        calculateOBP(player);
        calculateSlugging(player);
        calculateOPS(player);

        return hasStatChanged(player.getAverage(), oldAvg,
                player.getObp(), oldObp,
                player.getSlugging(), oldSlg,
                player.getOps(), oldOps);
    }

    private static boolean updatePitcherStats(Pitcher pitcher) {
        double oldEra = pitcher.getEra();
        double oldWhip = pitcher.getWhip();

        calculateERA(pitcher);
        calculateWHIP(pitcher);

        return hasStatChanged(pitcher.getEra(), oldEra,
                pitcher.getWhip(), oldWhip);
    }

    private static boolean hasStatChanged(double... values) {
        for (int i = 0; i < values.length; i += 2) {
            double newVal = values[i];
            double oldVal = values[i + 1];
            if (Double.compare(newVal, oldVal) != 0)
                return true;
        }
        return false;
    }

    private static void calculateAverage(PositionPlayer player) {
        int atBats = player.getAtBats();
        if (atBats == 0) {
            player.setAverage(0.0);
            return;
        }
        double avg = (double) player.getHits() / atBats;
        player.setAverage(truncateToThreeDecimals(avg));
    }

    private static void calculateOBP(PositionPlayer player) {
        int plateAppearances = player.getAtBats() + player.getWalks();
        if (plateAppearances == 0) {
            player.setObp(0.0);
            return;
        }
        double obp = (double) (player.getHits() + player.getWalks()) / plateAppearances;
        player.setObp(truncateToThreeDecimals(obp));
    }

    private static void calculateSlugging(PositionPlayer player) {
        int atBats = player.getAtBats();
        if (atBats == 0) {
            player.setSlugging(0.0);
            return;
        }
        int totalBases = player.getHits()
                + player.getDoubles()
                + 2 * player.getTriples()
                + 3 * player.getHomeRuns();
        double slg = (double) totalBases / atBats;
        player.setSlugging(truncateToThreeDecimals(slg));
    }

    private static void calculateOPS(PositionPlayer player) {
        double ops = player.getObp() + player.getSlugging();
        player.setOps(truncateToThreeDecimals(ops));
    }

    private static double convertInnings(double rawInnings) {
        int whole = (int) rawInnings;
        double decimal = rawInnings - whole;

        if (Math.abs(decimal - 0.1) < 0.01) {
            return whole + (1.0 / 3);
        } else if (Math.abs(decimal - 0.2) < 0.01) {
            return whole + (2.0 / 3);
        } else {
            return rawInnings;
        }
    }

    private static void calculateERA(Pitcher pitcher) {
        double innings = convertInnings(pitcher.getInningsPitched());
        if (innings == 0) {
            pitcher.setEra(0.0);
            return;
        }
        double era = (double) (pitcher.getRunsAllowed() * 9) / innings;
        pitcher.setEra(truncateToTwoDecimals(era));
    }

    private static void calculateWHIP(Pitcher pitcher) {
        double innings = convertInnings(pitcher.getInningsPitched());
        if (innings == 0) {
            pitcher.setWhip(0.0);
            return;
        }
        double whip = (double) (pitcher.getWalks() + pitcher.getHitsAllowed()) / innings;
        pitcher.setWhip(truncateToThreeDecimals(whip));
    }

    private static double truncateToThreeDecimals(double value) {
        return ((int) (value * 1000)) / 1000.0;
    }

    private static double truncateToTwoDecimals(double value) {
        return ((int) (value * 100)) / 100.0;
    }
}