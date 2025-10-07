package com.mlb.mlbportal.unit;

import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.VALID_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.mlb.mlbportal.handler.UserNotFoundException;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.EmailService;

class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordResetTokenRepository passwordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailService emailService;

    private UserEntity testUser;
    private PasswordResetToken token;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.testUser = new UserEntity(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);
        this.token = new PasswordResetToken(VALID_CODE, this.testUser);
    }

    @Test
    @DisplayName("getCode should return its respective token")
    void testGetCode() {
        when(this.passwordRepository.findByCode(VALID_CODE)).thenReturn(Optional.of(this.token));

        Optional<PasswordResetToken> resultToken = this.emailService.getCode(VALID_CODE);

        assertThat(resultToken).isPresent();
        assertThat(resultToken.get().getCode()).isEqualTo(VALID_CODE);
        assertThat(resultToken.get().getUser()).isEqualTo(this.testUser);
    }

    @Test
    @DisplayName("deleteToken should delete the token from te repository")
    void testDeleteToken() {
        this.emailService.deleteToken(this.token);
        verify(this.passwordRepository, times(1)).delete(this.token);
    }

    @Test
    @DisplayName("sendEmail should send an email with a 4 digit code, and save this code")
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
    @DisplayName("sendEmail should throw UserNotFoundException if the email is not registered in the database")
    void testSendEmailWithNonExistentUser() {
        when(this.userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.emailService.sendEmail(UNKNOWN_EMAIL))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("There is no user registered with this email");
        
        verify(this.passwordRepository, never()).save(any());
        verify(this.mailSender, never()).send(any(SimpleMailMessage.class));
    }
}