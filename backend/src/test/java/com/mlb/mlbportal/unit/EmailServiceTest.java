package com.mlb.mlbportal.unit;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.mlb.mlbportal.handler.notFound.UserNotFoundException;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.EmailService;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordResetTokenRepository passwordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private EmailService emailService;

    private UserEntity testUser;
    private PasswordResetToken token;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.testUser = new UserEntity(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);
        this.token = new PasswordResetToken(VALID_CODE, this.testUser);
    }

    @Test
    @DisplayName("Should return its respective token")
    void testGetCode() {
        when(this.passwordRepository.findByCode(VALID_CODE)).thenReturn(Optional.of(this.token));

        Optional<PasswordResetToken> resultToken = this.emailService.getCode(VALID_CODE);

        assertThat(resultToken).isPresent();
        assertThat(resultToken.get().getCode()).isEqualTo(VALID_CODE);
        assertThat(resultToken.get().getUser()).isEqualTo(this.testUser);
    }

    @Test
    @DisplayName("Should delete the token from te repository")
    void testDeleteToken() {
        this.emailService.deleteToken(this.token);
        verify(this.passwordRepository, times(1)).delete(this.token);
    }

    @Test
    @DisplayName("Should send an email with a 4 digit code, and save this code")
    void testSendEmail() {
        when(this.userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(this.testUser));
        when(this.passwordRepository.findByUser(this.testUser)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
           this.emailService.sendEmail(TEST_USER_EMAIL); 
        });

        verify(this.passwordRepository, times(1)).save(any(PasswordResetToken.class));
        verify(this.mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if the email is not registered in the database")
    void testSendEmailWithNonExistentUser() {
        when(this.userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.emailService.sendEmail(UNKNOWN_EMAIL))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("There is no user registered with this email");
        
        verify(this.passwordRepository, never()).save(any());
        verify(this.mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send email to fans with notifications enabled")
    void testSendDynamicGameReminderSuccess() {
        Long matchId = 1L;
        Match mockMatch = mock(Match.class);
        Team homeTeam = mock(Team.class);
        Team awayTeam = mock(Team.class);

        UserEntity fan1 = BuildMocksFactory.setUpUsers().getFirst();
        UserEntity fan2 = BuildMocksFactory.setUpUsers().get(1);
        fan1.setEnableNotifications(true);
        fan2.setEnableNotifications(false);

        Set<UserEntity> homeFans = new HashSet<>(Set.of(fan1, fan2));

        when(this.matchRepository.findById(matchId)).thenReturn(Optional.of(mockMatch));
        when(mockMatch.getHomeTeam()).thenReturn(homeTeam);
        when(mockMatch.getAwayTeam()).thenReturn(awayTeam);
        when(homeTeam.getFavoritedByUsers()).thenReturn(homeFans);
        when(awayTeam.getFavoritedByUsers()).thenReturn(new HashSet<>());
        when(mockMatch.getHomeTeam().getName()).thenReturn("Home Team");
        when(mockMatch.getAwayTeam().getName()).thenReturn("Away Team");

        this.emailService.sendDynamicGameReminder(matchId);
        verify(this.mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not send email if no fans have notifications enabled")
    void testSendDynamicGameReminderNoEnabledFans() {
        Long matchId = 1L;
        Match mockMatch = mock(Match.class);
        Team homeTeam = mock(Team.class);

        UserEntity fan1 = BuildMocksFactory.setUpUsers().getFirst();
        fan1.setEnableNotifications(false);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(mockMatch));
        when(mockMatch.getHomeTeam()).thenReturn(homeTeam);
        when(homeTeam.getFavoritedByUsers()).thenReturn(Set.of(fan1));

        this.emailService.sendDynamicGameReminder(matchId);
        verify(this.mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should do nothing if match is not found")
    void testSendDynamicGameReminderMatchNotFound() {
        when(this.matchRepository.findById(1L)).thenReturn(Optional.empty());

        this.emailService.sendDynamicGameReminder(1L);

        verify(this.mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send email with correct keywords for password change")
    void testSendProfileChangeNotificationPassword() {
        this.emailService.sendProfileChangeNotification(USER1_USERNAME, USER1_EMAIL, USER1_EMAIL, true);
        verify(this.mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}