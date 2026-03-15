package com.mlb.mlbportal.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.mlb.mlbportal.models.analytics.VisibilityStats;
import com.mlb.mlbportal.repositories.analytics.VisibilityStatsRepository;
import com.mlb.mlbportal.services.StatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {
    @Mock
    private VisibilityStatsRepository visibilityStatsRepository;

    @InjectMocks
    private StatsService statsService;

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
}