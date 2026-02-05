package com.mlb.mlbportal.schedulers;

import java.time.LocalDate;

import com.mlb.mlbportal.services.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mlb.mlbportal.services.MlbImportService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchScheduler {
    private final MlbImportService mlbImportService;
    private final MatchService matchService;

    @Scheduled(cron = "0 0 3 * * *")
    public void preloadMatches() {
        LocalDate now = LocalDate.now();
        LocalDate currentStart = now.withDayOfMonth(1);
        LocalDate currentEnd = currentStart.withDayOfMonth(currentStart.lengthOfMonth());

        LocalDate nextStart = currentStart.plusMonths(1);
        LocalDate nextEnd = nextStart.withDayOfMonth(nextStart.lengthOfMonth());

        this.mlbImportService.getOfficialMatches(currentStart, currentEnd);
        this.mlbImportService.getOfficialMatches(nextStart, nextEnd);
    }

    @Scheduled(cron = "0 0 19-23 * * *")
    @Scheduled(cron = "0 0 0-4 * * *")
    public void updateStandings() {
        log.info("Starting synchronization of the matches status");
        try {
            this.mlbImportService.verifyMatchStatus();
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
