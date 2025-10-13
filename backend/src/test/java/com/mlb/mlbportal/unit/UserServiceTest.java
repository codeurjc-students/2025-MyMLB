package com.mlb.mlbportal.unit;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.dto.user.UserRole;
import com.mlb.mlbportal.handler.UserAlreadyExistsException;
import com.mlb.mlbportal.mappers.AuthenticationMapper;
import com.mlb.mlbportal.mappers.UserMapper;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.UserService;

import static com.mlb.mlbportal.utils.TestConstants.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private UserEntity user1;
    private UserEntity user2;
    private UserEntity testUser;
    private PasswordResetToken validCode;
    private PasswordResetToken invalidCode;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.user1 = new UserEntity(USER1_EMAIL, USER1_USERNAME);
        this.user2 = new UserEntity(USER2_EMAIL, USER2_USERNAME);
        this.testUser = new UserEntity(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);

        this.validCode = new PasswordResetToken(VALID_CODE, this.testUser);
        this.validCode.setExpirationDate(LocalDateTime.now().plusMinutes(10));

        this.invalidCode = new PasswordResetToken(INVALID_CODE, this.testUser);
        this.invalidCode.setExpirationDate(LocalDateTime.now().minusMinutes(10));

        this.validCode.setUser(this.testUser);
        this.invalidCode.setUser(this.testUser);
        this.testUser.setResetToken(this.validCode);
    }

    @Test
    @DisplayName("getAllUsers should return all users from the repository")
    void testGetAllUsers() {
        List<UserEntity> mockUsers = Arrays.asList(user1, user2);
        when(this.userRepository.findAll()).thenReturn(mockUsers);

        List<ShowUser> mappedUsers = Arrays.asList(
                new ShowUser(USER1_USERNAME, USER1_EMAIL),
                new ShowUser(USER2_USERNAME, USER2_EMAIL));
        when(this.userMapper.toShowUsers(mockUsers)).thenReturn(mappedUsers);

        List<ShowUser> result = this.userService.getAllUsers();

        assertThat(result).hasSize(2);

        assertThat(result).extracting(ShowUser::username)
                .containsExactly(USER1_USERNAME, USER2_USERNAME);

        assertThat(result).extracting(ShowUser::email)
                .contains(USER1_EMAIL, USER2_EMAIL);
    }

    @Test
    @DisplayName("createUser should successfully create a new user if username does not exist")
    void testCreateNonExistantUser() {
        RegisterRequest registerRequest = new RegisterRequest(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);

        when(this.userRepository.findByUsername(TEST_USER_USERNAME)).thenReturn(Optional.empty());
        when(this.passwordEncoder.encode(TEST_USER_PASSWORD)).thenReturn("encodedPassword");

        when(this.authenticationMapper.toRegisterRequest(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity u = invocation.getArgument(0);
            return new RegisterRequest(u.getEmail(), u.getUsername(), u.getPassword());
        });

        RegisterRequest result = this.userService.createUser(registerRequest);

        assertThat(result.username()).isEqualTo(TEST_USER_USERNAME);
        assertThat(result.email()).isEqualTo(TEST_USER_EMAIL);
        assertThat(result.password()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("createUser should throw UserAlreadyExistsException if username already exists")
    void testCreateExistantUser() {
        RegisterRequest registerRequest = new RegisterRequest(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);

        when(this.userRepository.findByUsername(TEST_USER_USERNAME)).thenReturn(Optional.of(this.testUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, 
            () -> this.userService.createUser(registerRequest));

        assertThat(exception.getMessage()).isEqualTo("The User Already Exists in the Database");
    }

    @Test
    @DisplayName("resetPassword should return true and update the password with a valid code")
    void testResetPasswordWithValidToken() {
        when(this.emailService.getCode(VALID_CODE)).thenReturn(Optional.of(this.validCode));
        when(this.passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_ENCODED);

        boolean result = this.userService.resetPassword(VALID_CODE, NEW_PASSWORD);

        assertThat(result).isTrue();
        assertThat(this.testUser.getPassword()).isEqualTo(NEW_PASSWORD_ENCODED);
    }

    @Test
    @DisplayName("resetPassword should return false and not update the password with an invalid code")
    void testResetPasswordWithInvalidToken() {
        when(this.emailService.getCode(INVALID_CODE)).thenReturn(Optional.of(this.invalidCode));

        boolean result = this.userService.resetPassword(INVALID_CODE, NEW_PASSWORD);

        assertThat(result).isFalse();
        verify(this.userRepository, never()).save(any());
        verify(this.emailService, times(1)).deleteToken(this.invalidCode);
    }

    @Test
    @DisplayName("resetPassword should return false and not update the password with an empty code")
    void testResetPasswordWithEmptyToken() {
        when(this.emailService.getCode(INVALID_CODE)).thenReturn(Optional.empty());

        boolean result = this.userService.resetPassword(INVALID_CODE, NEW_PASSWORD);

        assertThat(result).isFalse();
        verify(this.userRepository, never()).save(any());
        verify(this.emailService, never()).deleteToken(this.invalidCode);
    }

    @Test
    @DisplayName("getUserRole should successfully return the UserRole object")
    void testGetUserRole() {
        UserRole mockUserRole = new UserRole(TEST_USER_USERNAME, List.of("USER"));

        when(this.userRepository.findByUsername(TEST_USER_USERNAME)).thenReturn(Optional.of(this.testUser));
        when(this.authenticationMapper.toUserRole(this.testUser)).thenReturn(mockUserRole);

        UserRole result = this.userService.getUserRole(TEST_USER_USERNAME);

        assertThat(result.username()).isEqualTo(TEST_USER_USERNAME);
        assertThat(result.roles()).contains("USER");
    }
}