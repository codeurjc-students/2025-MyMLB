package com.mlb.mlbportal.unit.scheduler;

import com.mlb.mlbportal.schedulers.PlayerScheduler;
import com.mlb.mlbportal.services.mlbAPI.PlayerImportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
class PlayerSchedulerTest {
    @Mock
    private PlayerImportService playerImportService;

    @InjectMocks
    private PlayerScheduler playerScheduler;

    @Test
    @DisplayName("Should call updateRostersAndPlayerStats each day at 6:00 am")
    void testUpdateRosterAndPlayerStats() throws NoSuchMethodException {
        Method method = PlayerScheduler.class.getMethod("updateRostersAndPlayerStats");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        this.playerScheduler.updateRostersAndPlayerStats();

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 6 * * *");
        verify(this.playerImportService, times(1)).getTeamRoster();
    }
}