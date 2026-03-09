package com.mlb.mlbportal.services;

import com.mlb.mlbportal.models.VisibilityStats;
import com.mlb.mlbportal.repositories.VisibilityStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final VisibilityStatsRepository visibilityStatsRepository;

    public List<VisibilityStats> getVisibilityStats(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null) {
            dateFrom = LocalDate.now();
        }
        if (dateTo == null) {
            dateTo = LocalDate.now().minusMonths(1);
        }
       return this.visibilityStatsRepository.findStatsByRange(dateFrom, dateTo);
    }

    public void increaseVisualizations() {
        this.visibilityStatsRepository.increaseVisualizations(LocalDate.now());
    }

    public void increaseRegistrations() {
        this.visibilityStatsRepository.increaseRegistrations(LocalDate.now());
    }
}