package com.mlb.mlbportal.unit;

import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.mlb.mlbportal.dto.analytics.APIAnalyticsDTO;
import com.mlb.mlbportal.dto.team.FavTeamAnalyticsDTO;
import com.mlb.mlbportal.mappers.analytics.APIAnalyticsMapper;
import com.mlb.mlbportal.models.analytics.APIPerformance;
import com.mlb.mlbportal.models.analytics.VisibilityStats;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.analytics.APIPerformanceRepository;
import com.mlb.mlbportal.repositories.analytics.VisibilityStatsRepository;
import com.mlb.mlbportal.services.AnalyticsService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock
    private VisibilityStatsRepository visibilityStatsRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private APIPerformanceRepository apiPerformanceRepository;

    @Mock
    private APIAnalyticsMapper apiAnalyticsMapper;

    @InjectMocks
    private AnalyticsService statsService;

    private final LocalDate now = LocalDate.now();

    @Test
    @DisplayName("Should return the stats within the default date range (1 month ago - now)")
    void testGetVisibilityStatsWithDefaultDateRange() {
        VisibilityStats defaultStats = new VisibilityStats(this.now, 10, 5, 2);
        LocalDate dateFrom = this.now.minusMonths(1);

        when(this.visibilityStatsRepository.findStatsByRange(dateFrom, this.now)).thenReturn(List.of(defaultStats));

        List<VisibilityStats> result = this.statsService.getVisibilityStats(null, null);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getVisualizations()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return the stats within the specified date range")
    void testGetVisibilityStatsWithDateRange() {
        LocalDate dateFrom = this.now.minusMonths(2);
        LocalDate dateTo = this.now.minusMonths(1);
        VisibilityStats inRangeStat = new VisibilityStats(this.now.minusMonths(1).minusWeeks(2), 4, 2, 1);

        when(this.visibilityStatsRepository.findStatsByRange(dateFrom, dateTo)).thenReturn(List.of(inRangeStat));

        List<VisibilityStats> result = this.statsService.getVisibilityStats(dateFrom, dateTo);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getVisualizations()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should update visualizations when record already exists for today")
    void testIncreaseVisualizationsUpdate() {
        when(this.visibilityStatsRepository.increaseVisualizations(now)).thenReturn(1);

        this.statsService.increaseVisualizations();

        verify(this.visibilityStatsRepository, times(1)).increaseVisualizations(now);
        verify(this.visibilityStatsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create new record when no stats exist for today")
    void testIncreaseNewUsersCreate() {
        when(this.visibilityStatsRepository.increaseNewUsers(now)).thenReturn(0);

        this.statsService.increaseNewUsers();

        verify(this.visibilityStatsRepository).increaseNewUsers(now);
        verify(this.visibilityStatsRepository).save(any(VisibilityStats.class));
    }

    @Test
    @DisplayName("Should handle race condition during record creation")
    void testIncreaseDeletedUsersRaceCondition() {
        when(this.visibilityStatsRepository.increaseDeletedUsers(now)).thenReturn(0).thenReturn(1);
        when(this.visibilityStatsRepository.save(any(VisibilityStats.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        this.statsService.increaseDeletedUsers();

        verify(this.visibilityStatsRepository, times(2)).increaseDeletedUsers(now);
        verify(this.visibilityStatsRepository, times(1)).save(any(VisibilityStats.class));
    }

    @Test
    @DisplayName("Should return the favorite teams with their number of fans")
    void testFavTeamsAnalytics() {
        FavTeamAnalyticsDTO dto = new FavTeamAnalyticsDTO(TEST_TEAM1_NAME, 10L);
        when(this.teamRepository.findAllFavoriteTeamsCounter()).thenReturn(List.of(dto));

        Map<String, Long> expectedResult = this.statsService.getFavTeamsAnalytics();

        assertThat(expectedResult.entrySet()).hasSize(1);
        assertThat(expectedResult).containsEntry(TEST_TEAM1_NAME, 10L);
    }

    @Test
    @DisplayName("Should obtain API metrics from the actuator")
    void testGetAPIPerformanceAnalytics() {
        Timer successTimer = mock(Timer.class);
        Timer errorTimer = mock(Timer.class);
        Timer actuatorTimer = mock(Timer.class);

        Timer.Id idSuccess = mock(Timer.Id.class);
        when(idSuccess.getTag("uri")).thenReturn("/api/data");
        when(idSuccess.getTag("status")).thenReturn("200");
        when(successTimer.getId()).thenReturn(idSuccess);
        when(successTimer.count()).thenReturn(10L);
        when(successTimer.totalTime(TimeUnit.MILLISECONDS)).thenReturn(1000.0);

        Timer.Id idError = mock(Timer.Id.class);
        when(idError.getTag("uri")).thenReturn("/api/data");
        when(idError.getTag("status")).thenReturn("500");
        when(errorTimer.getId()).thenReturn(idError);
        when(errorTimer.count()).thenReturn(2L);
        when(errorTimer.totalTime(TimeUnit.MILLISECONDS)).thenReturn(500.0);

        Timer.Id idActuator = mock(Timer.Id.class);
        when(idActuator.getTag("uri")).thenReturn("/actuator/health");
        when(actuatorTimer.getId()).thenReturn(idActuator);

        // Simulate the search in the MaterRegistry
        Search search = mock(Search.class);
        when(meterRegistry.find("http.server.requests")).thenReturn(search);
        when(search.timers()).thenReturn(List.of(successTimer, errorTimer, actuatorTimer));

        APIAnalyticsDTO result = this.statsService.getAPIPerformanceAnalytics();

        assertThat(result.totalRequests()).isEqualTo(12L); // 10 + 2 (filtered actuator)
        assertThat(result.totalErrors()).isEqualTo(2L);
        assertThat(result.totalSuccesses()).isEqualTo(10L);
        // Avg ---> (1000 + 500) / 12 = 125.0
        assertThat(result.averageResponseTime()).isEqualTo(125.0);
        assertThat(result.mostDemandedEndpoints()).hasSize(2);
        assertThat(result.mostDemandedEndpoints().getFirst().uri()).isEqualTo("/api/data");
    }

    @Test
    @DisplayName("Should obtain API performance history for a certain date range")
    void testGetAPIPerformanceHistoryWeeks() {
        String range = "1w";
        APIPerformance perf = new APIPerformance();
        APIAnalyticsDTO dto = BuildMocksFactory.buildAPIAnalyticsDTO().getFirst();

        when(this.apiPerformanceRepository.findAllByTimeStampAfterOrderByTimeStampAsc(any(LocalDateTime.class))).thenReturn(List.of(perf));
        when(this.apiAnalyticsMapper.toListAPIDTO(any())).thenReturn(List.of(dto));

        List<APIAnalyticsDTO> result = this.statsService.getAPIPerformanceHistory(range);

        assertThat(result).hasSize(1);
        verify(this.apiPerformanceRepository).findAllByTimeStampAfterOrderByTimeStampAsc(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should set default date range (1 hour) when invalid range")
    void testGetAPIPerformanceHistoryDefault() {
        when(this.apiPerformanceRepository.findAllByTimeStampAfterOrderByTimeStampAsc(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        this.statsService.getAPIPerformanceHistory("invalid-range");
        verify(this.apiPerformanceRepository).findAllByTimeStampAfterOrderByTimeStampAsc(any(LocalDateTime.class));
    }
}