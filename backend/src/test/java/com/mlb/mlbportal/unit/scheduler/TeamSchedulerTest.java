package com.mlb.mlbportal.unit.scheduler;

import com.mlb.mlbportal.schedulers.TeamScheduler;
import com.mlb.mlbportal.services.mlbAPI.TeamImportService;
import com.mlb.mlbportal.services.utilities.CacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamSchedulerTest {
    @Mock
    private TeamImportService teamImportService;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private TeamScheduler teamScheduler;

    @Test
    @DisplayName("Should call updateTeamStatsAndRankings each day at 5:00 am")
    void testUpdateRosterAndPlayerStats() throws NoSuchMethodException {
        Method method = TeamScheduler.class.getMethod("updateTeamStatsAndRankings");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        this.teamScheduler.updateTeamStatsAndRankings();

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 5 * * *");
        verify(this.teamImportService, times(1)).getTeamStats();
        verify(this.cacheService,times(1)).clearCaches(
                "get-teams",
                "get-standings",
                "runs-per-rival",
                "wins-per-rivals",
                "win-distribution",
                "historic-ranking",
                "search-team"
        );
    }
}