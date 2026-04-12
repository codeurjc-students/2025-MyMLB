package com.mlb.mlbportal.schedulers;

import com.mlb.mlbportal.services.mlbAPI.PlayerImportService;
import com.mlb.mlbportal.services.utilities.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerScheduler {
    private final PlayerImportService playerImportService;
    private final CacheService cacheService;

    @Async
    @Scheduled(cron = "0 0 6 * * *", zone = "Europe/Madrid")
    public void updateRostersAndPlayerStats() {
        this.playerImportService.getTeamRoster();
        this.cacheService.clearCaches(
                "get-players",
                "all-stats-player-rankings",
                "single-stat-player-rankings",
                "search-player"
        );
        log.info("Player Stats Updated. Reset Player Cache");
    }
}