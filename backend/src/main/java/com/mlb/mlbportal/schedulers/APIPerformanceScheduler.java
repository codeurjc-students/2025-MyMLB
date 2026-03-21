package com.mlb.mlbportal.schedulers;

import com.mlb.mlbportal.dto.analytics.APIAnalyticsDTO;
import com.mlb.mlbportal.mappers.analytics.APIAnalyticsMapper;
import com.mlb.mlbportal.repositories.analytics.APIPerformanceRepository;
import com.mlb.mlbportal.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class APIPerformanceScheduler {
    private final AnalyticsService analyticsService;
    private final APIPerformanceRepository apiPerformanceRepository;
    private final APIAnalyticsMapper apiAnalyticsMapper;

    @Scheduled(fixedRate = 60000) // each min
    @Transactional
    public void retrievePerformanceData() {
        APIAnalyticsDTO currentData = this.analyticsService.getAPIPerformanceAnalytics();
        this.apiPerformanceRepository.save(this.apiAnalyticsMapper.toDomain(currentData));
    }
}