package com.mlb.mlbportal.schedulers;

import com.mlb.mlbportal.services.mlbAPI.TeamImportService;
import com.mlb.mlbportal.services.utilities.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamScheduler {
    private final TeamImportService teamImportService;
    private final CacheService cacheService;

    @Async
    @Scheduled(cron = "0 0 5 * * *", zone = "Europe/Madrid")
    public void updateTeamStatsAndRankings() {
        this.teamImportService.getTeamStats();
        this.cacheService.clearCaches(
                "get-teams",
                "get-standings",
                "runs-per-rival",
                "wins-per-rivals",
                "win-distribution",
                "historic-ranking"
        );
        log.info("Team Stats Updated. Reset Team Cache");
    }
}