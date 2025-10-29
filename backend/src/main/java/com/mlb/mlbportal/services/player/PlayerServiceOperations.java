package com.mlb.mlbportal.services.player;

import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.Player;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;

public class PlayerServiceOperations {
    protected static void updatePlayerStats(Player player,
            PositionPlayerRepository positionPlayerRepo,
            PitcherRepository pitcherRepo) {
        switch (player) {
            case PositionPlayer positionPlayer -> updatePositionPlayerStats(positionPlayer, positionPlayerRepo);
            case Pitcher pitcher -> updatePitcherStats(pitcher, pitcherRepo);
            default -> {
            }
        }
    }

    private static void updatePositionPlayerStats(PositionPlayer player, PositionPlayerRepository repo) {
        double oldAverage = player.getAverage();
        double oldOBP = player.getObp();
        double oldSlugging = player.getSlugging();
        double oldOPS = player.getOps();

        calculateAverage(player);
        calculateOBP(player);
        calculateSlugging(player);
        calculateOPS(player);

        if (statsChanged(player.getAverage(), oldAverage,
                player.getObp(), oldOBP,
                player.getSlugging(), oldSlugging,
                player.getOps(), oldOPS)) {
            repo.save(player);
        }
    }

    private static void updatePitcherStats(Pitcher pitcher,
            PitcherRepository repo) {
        double oldERA = pitcher.getEra();
        double oldWHIP = pitcher.getWhip();

        calculateERA(pitcher);
        calculateWHIP(pitcher);

        if (statsChanged(pitcher.getEra(), oldERA,
                pitcher.getWhip(), oldWHIP)) {
            repo.save(pitcher);
        }
    }

    private static boolean statsChanged(double... values) {
        for (int i = 0; i < values.length; i += 2) {
            if (Double.compare(values[i], values[i + 1]) != 0)
                return true;
        }
        return false;
    }

    private static void calculateAverage(PositionPlayer player) {
        double avg = (double) player.getHits() / player.getAtBats();
        player.setAverage(((int) (avg * 1000)) / 1000.0);
    }

    private static void calculateOBP(PositionPlayer player) {
        double obp = (double) (player.getHits() + player.getWalks()) / (player.getAtBats() + player.getWalks());
        player.setObp(((int) (obp * 1000)) / 1000.0);
    }

    private static void calculateSlugging(PositionPlayer player) {
        double slg = (player.getHits() + 2 * player.getDoubles() + 3 * player.getTriples() + 4 * player.getHomeRuns())
                / (double) player.getAtBats();
        player.setSlugging(((int) (slg * 1000)) / 1000.0);
    }

    private static void calculateOPS(PositionPlayer player) {
        player.setOps(player.getObp() + player.getSlugging());
    }

    private static void calculateERA(Pitcher pitcher) {
        double era = (double) (pitcher.getRunsAllowed() * 9) / pitcher.getInningsPitched();
        pitcher.setEra(((int) (era * 100)) / 100.0);
    }

    private static void calculateWHIP(Pitcher pitcher) {
        double whip = (double) (pitcher.getWalks() + pitcher.getHitsAllowed()) / pitcher.getInningsPitched();
        pitcher.setWhip(((int) (whip * 1000)) / 1000.0);
    }
}