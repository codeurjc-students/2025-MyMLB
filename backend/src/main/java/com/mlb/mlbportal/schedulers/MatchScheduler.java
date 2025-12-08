package com.mlb.mlbportal.schedulers;

import com.mlb.mlbportal.services.MlbImportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MatchScheduler {

    private final MlbImportService mlbImportService;

    public MatchScheduler(MlbImportService mlbImportService) {
        this.mlbImportService = mlbImportService;
    }

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
}
