package com.mlb.mlbportal.services;

import com.mlb.mlbportal.dto.analytics.APIAnalyticsDTO;
import com.mlb.mlbportal.dto.analytics.EndpointAnalyticsDTO;
import com.mlb.mlbportal.dto.team.FavTeamAnalyticsDTO;
import com.mlb.mlbportal.mappers.analytics.APIAnalyticsMapper;
import com.mlb.mlbportal.models.analytics.APIPerformance;
import com.mlb.mlbportal.models.analytics.VisibilityStats;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.analytics.APIPerformanceRepository;
import com.mlb.mlbportal.repositories.analytics.VisibilityStatsRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final VisibilityStatsRepository visibilityStatsRepository;
    private final TeamRepository teamRepository;
    private final APIPerformanceRepository apiPerformanceRepository;
    private final MeterRegistry meterRegistry;
    private final APIAnalyticsMapper apiAnalyticsMapper;

    // --------- Visibility Analytics ----------------------------------
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
    private void manageUpdate(ToIntFunction<LocalDate> repositoryFunc, String type) {
        LocalDate today = LocalDate.now();
        int rowsUpdated = repositoryFunc.applyAsInt(today);
        if (rowsUpdated == 0) {
            try {
                switch (type) {
                    case "visualizations" -> this.visibilityStatsRepository.save(new VisibilityStats(today, 1, 0, 0));
                    case "newUsers" -> this.visibilityStatsRepository.save(new VisibilityStats(today, 0, 1, 0));
                    case "deletedUsers" -> this.visibilityStatsRepository.save(new VisibilityStats(today, 0, 0, 1));
                }
            }
            catch (DataIntegrityViolationException ex) {
                repositoryFunc.applyAsInt(today);
            }
        }
    }

    // --------- Fav Teams Analytics ----------------------------------
    @Transactional(readOnly = true)
    public Map<String, Long> getFavTeamsAnalytics() {
        List<FavTeamAnalyticsDTO> queryResult = this.teamRepository.findAllFavoriteTeamsCounter();
        Map<String, Long> result = new HashMap<>();
        queryResult.forEach(dto -> result.put(dto.teamName(), dto.counter()));
        return result;
    }

    // --------- API Analytics ----------------------------------
    public APIAnalyticsDTO getAPIPerformanceAnalytics() {
        var requestsSearch = this.meterRegistry.find("http.server.requests");

        var filteredTimers = requestsSearch.timers().stream().filter(t -> {
            String uri = t.getId().getTag("uri");
            return uri != null && !uri.contains("actuator") && !uri.equals("/**") && !uri.equals("/error");
        }).toList();

        long totalRequests = filteredTimers.stream().mapToLong(Timer::count).sum();

        long totalErrors = filteredTimers.stream().filter(timer -> {
           String status = timer.getId().getTag("status");
           return status != null && (status.startsWith("4") || status.startsWith("5"));
        }).mapToLong(Timer::count).sum();

        long totalSuccess = totalRequests - totalErrors;

        double totalTime = filteredTimers.stream().mapToDouble(timer -> timer.totalTime(TimeUnit.MILLISECONDS)).sum();

        double averageTime = (totalRequests > 0) ? (totalTime / totalRequests) : 0.0;

        List<EndpointAnalyticsDTO> topEndpoints = filteredTimers.stream().map(timer -> new EndpointAnalyticsDTO(timer.getId().getTag("uri"), timer.count()))
                .filter(end -> !end.uri().equals("/**"))
                .sorted((e1, e2) -> Long.compare(e2.count(), e1.count()))
                .limit(10)
                .toList();

        return new APIAnalyticsDTO(
                LocalDateTime.now(),
                totalRequests,
                totalErrors,
                totalSuccess,
                Math.round(averageTime * 100.0) / 100.0,
                topEndpoints
        );
    }

    @Transactional(readOnly = true)
    public List<APIAnalyticsDTO> getAPIPerformanceHistory(String dateRange) {
        LocalDateTime dateFrom = this.getDateFrom(dateRange);
        List<APIPerformance> queryResult = this.apiPerformanceRepository.findAllByTimeStampAfterOrderByTimeStampAsc(dateFrom);
        return this.apiAnalyticsMapper.toListAPIDTO(queryResult);
    }

    private LocalDateTime getDateFrom(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        return switch (dateRange) {
            case "1d" -> now.minusDays(1);
            case "1w" -> now.minusWeeks(1);
            case "1m" -> now.minusMonths(1);
            default -> now.minusHours(1);
        };
    }
}