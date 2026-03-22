package com.mlb.mlbportal.schedulers;

import com.mlb.mlbportal.dto.analytics.APIAnalyticsDTO;
import com.mlb.mlbportal.mappers.analytics.APIAnalyticsMapper;
import com.mlb.mlbportal.repositories.analytics.APIPerformanceRepository;
import com.mlb.mlbportal.services.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class APIPerformanceScheduler {
    private final AnalyticsService analyticsService;
    private final APIPerformanceRepository apiPerformanceRepository;
    private final APIAnalyticsMapper apiAnalyticsMapper;

    private long lastTotalRequests = 0;
    private long lastTotalErrors = 0;

    public APIPerformanceScheduler(AnalyticsService analyticsService, APIPerformanceRepository apiPerformanceRepository, APIAnalyticsMapper apiAnalyticsMapper) {
        this.analyticsService = analyticsService;
        this.apiPerformanceRepository = apiPerformanceRepository;
        this.apiAnalyticsMapper = apiAnalyticsMapper;
    }

    @Scheduled(fixedRate = 60000) // each min
    @Transactional
    public void retrievePerformanceData() {
        APIAnalyticsDTO currentData = this.analyticsService.getAPIPerformanceAnalytics();
        long requestsThisMinute = Math.max(0, currentData.totalRequests() - this.lastTotalRequests);
        long errorsThisMinute = Math.max(0, currentData.totalErrors() - this.lastTotalErrors);
        long successThisMinute = requestsThisMinute - errorsThisMinute;

        this.lastTotalRequests = currentData.totalRequests();
        this.lastTotalErrors = currentData.totalErrors();

        APIAnalyticsDTO minuteSnapshot = new APIAnalyticsDTO(
                currentData.timeStamp(),
                requestsThisMinute,
                errorsThisMinute,
                successThisMinute,
                currentData.averageResponseTime(),
                currentData.mostDemandedEndpoints()
        );
        this.apiPerformanceRepository.save(this.apiAnalyticsMapper.toDomain(minuteSnapshot));
    }
}