package com.mlb.mlbportal.schedulers;

import com.mlb.mlbportal.services.mlbAPI.PlayerImportService;
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

    @Async
    @Scheduled(cron = "0 0 6 * * *")
    public void updateRostersAndPlayerStats() {
        this.playerImportService.getTeamRoster();
    }
}