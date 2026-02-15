package com.mlb.mlbportal.unit.scheduler;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.schedulers.MatchScheduler;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.mlbAPI.MatchImportService;
import org.springframework.scheduling.annotation.Scheduled;

@ExtendWith(MockitoExtension.class)
class MatchSchedulerTest {

    @Mock
    private MatchImportService mlbImportService;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchScheduler matchScheduler;

    @Test
    @DisplayName("Should calculate current and next month ranges correctly")
    void testPreloadMatches() throws NoSuchMethodException {
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        Method method = MatchScheduler.class.getMethod("preloadMatches");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

       this.matchScheduler.preloadMatches();

        verify(this.mlbImportService, times(2)).getOfficialMatches(startCaptor.capture(), endCaptor.capture());

        List<LocalDate> capturedStarts = startCaptor.getAllValues();
        List<LocalDate> capturedEnds = endCaptor.getAllValues();

        LocalDate now = LocalDate.now();
        assertThat(capturedStarts.getFirst()).isEqualTo(now.withDayOfMonth(1));
        assertThat(capturedEnds.getFirst()).isEqualTo(now.withDayOfMonth(now.lengthOfMonth()));

        LocalDate nextMonth = now.plusMonths(1);
        assertThat(capturedStarts.get(1)).isEqualTo(nextMonth.withDayOfMonth(1));
        assertThat(capturedEnds.get(1)).isEqualTo(nextMonth.withDayOfMonth(nextMonth.lengthOfMonth()));

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo( "0 0 3 * * *");
    }

    @Test
    @DisplayName("Should call verifyMatchStatus each day between 7pm and 4 am")
    void testUpdateStandings() throws NoSuchMethodException {
        Method method = MatchScheduler.class.getMethod("updateStandings");
        Scheduled[] scheduled = method.getAnnotationsByType(Scheduled.class);
        List<String> crons = Arrays.stream(scheduled).map(Scheduled::cron).toList();

        this.matchScheduler.updateStandings();

        assertThat(scheduled).hasSize(2);
        assertThat(crons).containsExactlyInAnyOrder("0 0 19-23 * * *", "0 0 0-4 * * *");
        verify(this.mlbImportService, times(1)).verifyMatchStatus();
    }

    @Test
    @DisplayName("Should call the email notification method every 10mins")
    void testCheckSendingEmailNotification() throws NoSuchMethodException {
        Method method = MatchScheduler.class.getMethod("checkSendingEmailNotification");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        this.matchScheduler.checkSendingEmailNotification();

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 */10 * * * *");
        verify(this.matchService, times(1)).notificateMatchStart();
    }
}