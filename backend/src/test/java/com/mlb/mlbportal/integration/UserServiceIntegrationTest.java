package com.mlb.mlbportal.integration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mlb.mlbportal.dto.User.ShowUser;
import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.handler.UserAlreadyExistsException;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.UserService;
import static com.mlb.mlbportal.utils.TestConstants.INVALID_CODE;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.USER1_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER1_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.USER2_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER2_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.USER2_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.VALID_CODE;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity user1;
    private UserEntity user2;

    private PasswordResetToken validToken;
    private PasswordResetToken invalidToken;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        userRepository.deleteAll();

        this.user1 = new UserEntity(USER1_EMAIL, USER1_USERNAME, this.passwordEncoder.encode(USER1_PASSWORD));
        this.user2 = new UserEntity(USER2_EMAIL, USER2_USERNAME, this.passwordEncoder.encode(USER2_PASSWORD));

        this.userRepository.saveAll(List.of(this.user1, this.user2));

        this.validToken = new PasswordResetToken(VALID_CODE, this.user1);
        this.validToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        this.passwordRepository.save(this.validToken);

        this.invalidToken = new PasswordResetToken(INVALID_CODE, this.user2);
        this.invalidToken.setExpirationDate(LocalDateTime.now().minusMinutes(10));
        this.passwordRepository.save(this.invalidToken);
    }

    @Test
    @DisplayName("getAllUsers should return all users from the database")
    void testGetAllUsers() {
        List<ShowUser> result = this.userService.getAllUsers();

        assertThat(result).hasSize(2).extracting(ShowUser::username)
            .containsExactlyInAnyOrder(USER1_USERNAME, USER2_USERNAME);
    }

    @Test
    @DisplayName("createUser should persist a new user with encoded password")
    void testCreateUser() {
        RegisterRequest request = new RegisterRequest(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);

        RegisterRequest result = this.userService.createUser(request);

        assertThat(result.username()).isEqualTo(TEST_USER_USERNAME);
        assertThat(result.email()).isEqualTo(TEST_USER_EMAIL);
        assertThat(passwordEncoder.matches(TEST_USER_PASSWORD, result.password())).isTrue();

        UserEntity saved = this.userRepository.findByUsername(TEST_USER_USERNAME).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo(TEST_USER_EMAIL);
    }

    @Test
    @DisplayName("createUser should throw exception if user already exists")
    void testCreateExistingUser() {
        RegisterRequest request = new RegisterRequest(USER1_EMAIL, USER1_USERNAME, USER1_PASSWORD);

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(request);
        });
        assertThat(ex.getMessage()).isEqualTo("The User Already Exists on the Database");
    }
}