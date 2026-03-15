package com.mlb.mlbportal.services;

import com.mlb.mlbportal.models.analytics.VisibilityStats;
import com.mlb.mlbportal.repositories.analytics.VisibilityStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

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
        this.manageUpdate(this.visibilityStatsRepository::increaseVisualizations, "visualizations");
    }

    @Transactional
    public void increaseNewUsers() {
        this.manageUpdate(this.visibilityStatsRepository::increaseNewUsers, "newUsers");
    }

    @Transactional
    public void increaseDeletedUsers() {
        this.manageUpdate(this.visibilityStatsRepository::increaseDeletedUsers, "deletedUsers");
    }

    /**
     * Updates the existing record or creates a new one for today.Catches race conditions to ensure data consistency.
     * Encapsulates the common modification logic of the previous methods (DRY principle).
     *
     * @param repositoryFunc The repository increment function.
     * @param type The metric type to initialize if record is new.
     */
    private void manageUpdate(Function<LocalDate, Integer> repositoryFunc, String type) {
        LocalDate today = LocalDate.now();
        int rowsUpdated = repositoryFunc.apply(today);
        if (rowsUpdated == 0) {
            try {
                switch (type) {
                    case "visualizations" -> this.visibilityStatsRepository.save(new VisibilityStats(today, 1, 0, 0));
                    case "newUsers" -> this.visibilityStatsRepository.save(new VisibilityStats(today, 0, 1, 0));
                    case "deletedUsers" -> this.visibilityStatsRepository.save(new VisibilityStats(today, 0, 0, 1));
                }
            }
            catch (DataIntegrityViolationException ex) {
                repositoryFunc.apply(today);
            }
        }
    }
}