package com.mlb.mlbportal.services;

import com.mlb.mlbportal.models.VisibilityStats;
import com.mlb.mlbportal.repositories.VisibilityStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final VisibilityStatsRepository visibilityStatsRepository;

    @Transactional(readOnly = true)
    public List<VisibilityStats> getVisibilityStats(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusMonths(1);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }
       return this.visibilityStatsRepository.findStatsByRange(dateFrom, dateTo);
    }

    @Transactional
    public void increaseVisualizations() {
        this.visibilityStatsRepository.increaseVisualizations(LocalDate.now());
    }

    @Transactional
    public void increaseRegistrations() {
        this.visibilityStatsRepository.increaseRegistrations(LocalDate.now());
    }

    @Transactional
    public void increaseLosses() {
        this.visibilityStatsRepository.increaseLosses(LocalDate.now());
    }
}