package com.mlb.mlbportal.integration;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.models.analytics.VisibilityStats;
import com.mlb.mlbportal.repositories.analytics.VisibilityStatsRepository;
import com.mlb.mlbportal.services.StatsService;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StatsServiceIntegrationTest {
    @Autowired
    private VisibilityStatsRepository visibilityStatsRepository;

    @Autowired
    private StatsService statsService;

    private final LocalDate now = LocalDate.now();

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        VisibilityStats defaultStats = new VisibilityStats(this.now, 10, 6, 3);
        VisibilityStats outRangeStat = new VisibilityStats(this.now.minusMonths(4), 19, 2 ,4);
        this.visibilityStatsRepository.saveAll(List.of(defaultStats, outRangeStat));
    }

    @Test
    @DisplayName("Should return the stats within the date range")
    void testGetVisibilityStats() {
        List<VisibilityStats> result = this.statsService.getVisibilityStats(null, null);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getVisualizations()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should increase the amount of visualizations")
    void testIncreaseVisualizations() {
        this.statsService.increaseVisualizations();

        VisibilityStats stat = this.visibilityStatsRepository.findById(this.now).orElseThrow(NoSuchElementException::new);

        assertThat(stat).isNotNull();
        assertThat(stat.getVisualizations()).isEqualTo(11);
    }

    @Test
    @DisplayName("Should increase the amount of new users")
    void testIncreaseNewUsers() {
        this.statsService.increaseNewUsers();

        VisibilityStats stat = this.visibilityStatsRepository.findById(this.now).orElseThrow(NoSuchElementException::new);

        assertThat(stat).isNotNull();
        assertThat(stat.getNewUsers()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should increase the amount of deleted users")
    void testIncreaseDeletedUsers() {
        this.statsService.increaseDeletedUsers();

        VisibilityStats stat = this.visibilityStatsRepository.findById(this.now).orElseThrow(NoSuchElementException::new);

        assertThat(stat).isNotNull();
        assertThat(stat.getDeletedUsers()).isEqualTo(4);
    }
}