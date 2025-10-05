package com.mlb.mlbportal.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.handler.UserNotFoundException;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.EmailService;

import static com.mlb.mlbportal.utils.TestConstants.*;

@SpringBootTest
@Transactional
class EmailServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordRepository;

    @Autowired
    private EmailService emailService;

    private UserEntity testUser;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.passwordRepository.deleteAll();
        this.userRepository.deleteAll();

        this.testUser = new UserEntity(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);
        this.userRepository.save(this.testUser);
    }

    @Test
    @DisplayName("sendEmail should create a 4 digit token for a registered user")
    void testSendEmailWithRegisteredUser() {
        this.emailService.sendEmail(this.testUser.getEmail());

        Optional<PasswordResetToken> tokenOpt = this.passwordRepository.findByUser(this.testUser);
        assertThat(tokenOpt).isPresent();

        PasswordResetToken token = tokenOpt.get();
        assertThat(token.getCode()).matches("\\d{4}");
        assertThat(token.getUser().getEmail()).isEqualTo(this.testUser.getEmail());
    }

    @Test
    @DisplayName("sendEmail should throw exception if the user is not registered")
    void testSendEmailWithNoExistentUser() {
        assertThatThrownBy(() -> this.emailService.sendEmail(UNKNOWN_EMAIL))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("There is no user registered with this email");
    }
}