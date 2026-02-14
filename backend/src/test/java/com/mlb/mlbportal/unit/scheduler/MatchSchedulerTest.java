package com.mlb.mlbportal.unit;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.schedulers.MatchScheduler;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.mlbAPI.MatchImportService;

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
    void testPreloadMatches() {
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

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
    }

    @Test
    @DisplayName("Should call verifyMatchStatus")
    void testUpdateStandings() {
        this.matchScheduler.updateStandings();
        verify(this.mlbImportService, times(1)).verifyMatchStatus();

        doThrow(new RuntimeException("API Connection Failed")).when(this.mlbImportService).verifyMatchStatus();

        this.matchScheduler.updateStandings();

        verify(this.mlbImportService, times(2)).verifyMatchStatus();
    }

    @Test
    @DisplayName("Should call the email notification method")
    void testCheckSendingEmailNotification() {
        this.matchScheduler.checkSendingEmailNotification();
        verify(this.matchService, times(1)).notificateMatchStart();
    }
}