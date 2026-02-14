package com.mlb.mlbportal.schedulers;

import java.time.LocalDate;

import com.mlb.mlbportal.services.mlbAPI.MatchImportService;
import com.mlb.mlbportal.services.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchScheduler {
    private final MatchImportService matchImportService;
    private final MatchService matchService;

    @Scheduled(cron = "0 0 3 * * *")
    public void preloadMatches() {
        LocalDate now = LocalDate.now();
        LocalDate currentStart = now.withDayOfMonth(1);
        LocalDate currentEnd = currentStart.withDayOfMonth(currentStart.lengthOfMonth());

        LocalDate nextStart = currentStart.plusMonths(1);
        LocalDate nextEnd = nextStart.withDayOfMonth(nextStart.lengthOfMonth());

        this.matchImportService.getOfficialMatches(currentStart, currentEnd);
        this.matchImportService.getOfficialMatches(nextStart, nextEnd);
    }

    @Schedules({
            @Scheduled(cron = "0 0 19-23 * * *"),
            @Scheduled(cron = "0 0 0-4 * * *")
    })
    public void updateStandings() {
        log.info("Starting synchronization of the matches status");
        try {
            this.matchImportService.verifyMatchStatus();
        }
        catch (Exception ex) {
            log.error("An error occur while synchronizing the matches status: {}", ex.getMessage());
        }
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void checkSendingEmailNotification() {
        this.matchService.notificateMatchStart();
    }
}