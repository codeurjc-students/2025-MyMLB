package com.mlb.mlbportal.unit.scheduler;

import com.mlb.mlbportal.dto.analytics.APIAnalyticsDTO;
import com.mlb.mlbportal.mappers.analytics.APIAnalyticsMapper;
import com.mlb.mlbportal.repositories.analytics.APIPerformanceRepository;
import com.mlb.mlbportal.schedulers.APIPerformanceScheduler;
import com.mlb.mlbportal.services.AnalyticsService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class APIPerformanceSchedulerTest {

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private APIPerformanceRepository apiPerformanceRepository;

    @Mock
    private APIAnalyticsMapper apiAnalyticsMapper;

    @InjectMocks
    private APIPerformanceScheduler scheduler;

    @Test
    @DisplayName("Should calculate differential metrics (delta) correctly between two executions")
    void testRetrievePerformanceDataDifferential() {
        // First Execution
        APIAnalyticsDTO firstData = BuildMocksFactory.buildAPIAnalyticsDTO().getFirst();
        when(this.analyticsService.getAPIPerformanceAnalytics()).thenReturn(firstData);

        this.scheduler.retrievePerformanceData();

        verify(this.apiPerformanceRepository, times(1)).save(any());

        // Second Execution (Delta)
        APIAnalyticsDTO secondData = BuildMocksFactory.buildAPIAnalyticsDTO().get(1);
        when(this.analyticsService.getAPIPerformanceAnalytics()).thenReturn(secondData);

        this.scheduler.retrievePerformanceData();

        ArgumentCaptor<APIAnalyticsDTO> captor = ArgumentCaptor.forClass(APIAnalyticsDTO.class);
        verify(this.apiAnalyticsMapper, times(2)).toDomain(captor.capture());

        APIAnalyticsDTO lastSaved = captor.getAllValues().get(1);

        assertThat(lastSaved.totalRequests()).isEqualTo(50L); // secondData.totalRequests - firstData.totalRequests
        assertThat(lastSaved.totalErrors()).isEqualTo(5L);    // secondData.totalErrors - firstData.totalErrors
        assertThat(lastSaved.totalSuccesses()).isEqualTo(45L);  // secondData.totalSuccess - firstData.totalSuccess
    }

    @Test
    @DisplayName("Should handle counter reset")
    void testHandleCounterReset() {
        when(this.analyticsService.getAPIPerformanceAnalytics()).thenReturn(BuildMocksFactory.buildAPIAnalyticsDTO().getFirst());
        this.scheduler.retrievePerformanceData();

        when(this.analyticsService.getAPIPerformanceAnalytics()).thenReturn(BuildMocksFactory.buildAPIAnalyticsDTO().get(1));
        this.scheduler.retrievePerformanceData();

        ArgumentCaptor<APIAnalyticsDTO> captor = ArgumentCaptor.forClass(APIAnalyticsDTO.class);
        verify(this.apiAnalyticsMapper, times(2)).toDomain(captor.capture());

        assertThat(captor.getAllValues().get(1).totalRequests()).isEqualTo(50L);
    }
}