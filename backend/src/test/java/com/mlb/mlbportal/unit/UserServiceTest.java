package com.mlb.mlbportal.unit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mlb.mlbportal.dto.user.EditProfileRequest;
import com.mlb.mlbportal.handler.notFound.UserNotFoundException;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.services.uploader.PictureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.dto.user.UserRole;
import com.mlb.mlbportal.handler.conflict.TeamAlreadyExistsException;
import com.mlb.mlbportal.handler.conflict.UserAlreadyExistsException;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.AuthenticationMapper;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.mappers.UserMapper;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.UserService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    @SuppressWarnings("unused")
    private UserMapper userMapper;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private PaginationHandlerService paginationHandlerService;

    @Mock
    private PictureService pictureService;

    @InjectMocks
    private UserService userService;

    private UserEntity user1;
    private UserEntity user2;
    private UserEntity testUser;
    private PasswordResetToken validCode;
    private PasswordResetToken invalidCode;
    private List<Team> teams;
    private List<TeamSummary> teamSummaries;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
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

        this.teams = BuildMocksFactory.setUpTeamMocks();
        this.user1.setFavTeams(new HashSet<>(List.of(this.teams.get(0), this.teams.get(1))));

        this.teamSummaries = BuildMocksFactory.buildTeamSummaryMocks(this.teams);
    }

    @Test
    @DisplayName("getAllUsers should return all users from the repository")
    void testGetAllUsers() {
        List<UserEntity> users = Arrays.asList(user1, user2, testUser);
        List<ShowUser> mockUsers = BuildMocksFactory.buildShowUserDTOs(users);
        Page<ShowUser> mockPage = new PageImpl<>(mockUsers, PageRequest.of(0, 10), mockUsers.size());

        when(this.userRepository.findAll()).thenReturn(users);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(users), eq(0), eq(10), any());

        Page<ShowUser> result = this.userService.getAllUsers(0, 10);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(ShowUser::username).containsExactly(USER1_USERNAME, USER2_USERNAME, TEST_USER_USERNAME);
        assertThat(result.getContent()).extracting(ShowUser::email).contains(USER1_EMAIL, USER2_EMAIL, TEST_USER_EMAIL);
    }

    @Test
    @DisplayName("createUser should successfully create a new user if username does not exist")
    void testCreateNonExistentUser() {
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
    void testCreateExistentUser() {
        RegisterRequest registerRequest = new RegisterRequest(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);

        when(this.userRepository.findByUsername(TEST_USER_USERNAME)).thenReturn(Optional.of(this.testUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, 
            () -> this.userService.createUser(registerRequest));

        assertThat(exception.getMessage()).isEqualTo("The User Already Exists in the Database");
    }

    @Test
    @DisplayName("Should delete the user's account successfully")
    void testDeleteAccount() {
        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        assertThatNoException().isThrownBy(() -> this.userService.deleteAccount(USER1_USERNAME));
        verify(this.userRepository, times(1)).delete(this.user1);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when the user does not exists")
    void testDeleteAccountError(){
        when(this.userRepository.findByUsername(UNKNOWN_USER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> this.userService.deleteAccount(UNKNOWN_USER))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User Not Found");
    }

    @Test
    @DisplayName("Should edit the active user's profile correctly")
    void testEditProfile() {
        EditProfileRequest request = new EditProfileRequest(NEW_EMAIL, NEW_PASSWORD);
        ShowUser user = new ShowUser(USER1_USERNAME, NEW_EMAIL);

        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        when(this.passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_ENCODED);
        when(this.userMapper.toShowUser(this.user1)).thenReturn(user);

        ShowUser editedUser = this.userService.updateProfile(USER1_USERNAME, request);

        assertThat(editedUser.email()).isEqualTo(NEW_EMAIL);
        verify(this.userRepository, times(1)).save(this.user1);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if the username does not exists")
    void testInvalidEditProfile() {
        EditProfileRequest request = new EditProfileRequest(NEW_EMAIL, NEW_PASSWORD);

        when(this.userRepository.findByUsername(UNKNOWN_USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.userService.updateProfile(UNKNOWN_USER, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User Not Found");
    }

    @Test
    @DisplayName("Should change the profile picture of the active user successfully")
    void testUpdateProfilePicture() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        PictureInfo picture = new PictureInfo("http://cloudinary.com/test123.jpg", "test123");

        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        when(this.pictureService.uploadPicture(mockFile)).thenReturn(picture);

        PictureInfo result = this.userService.changeProfilePicture(USER1_USERNAME, mockFile);

        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isEqualTo(picture.getUrl());
        assertThat(result.getPublicId()).isEqualTo(picture.getPublicId());
        verify(this.userRepository, times(1)).save(this.user1);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if the username does not exists")
    void testInvalidUpdateProfilePicture() {
        MultipartFile mockFile = mock(MultipartFile.class);

        when(this.userRepository.findByUsername(UNKNOWN_USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.userService.changeProfilePicture(UNKNOWN_USER, mockFile))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User Not Found");
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

    @Test
    @DisplayName("Should return the favourite teams of a certain user")
    void testGetFavTeams() {
        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        Set<TeamSummary> expectedResult = new HashSet<>(List.of(this.teamSummaries.get(0), this.teamSummaries.get(1)));
        when(this.teamMapper.toTeamSummarySet(new HashSet<>(List.of(this.teams.get(0), this.teams.get(1))))).thenReturn(expectedResult);

        Set<TeamSummary> result = this.userService.getFavTeamsOfAUser(USER1_USERNAME);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TeamSummary::name).containsExactlyInAnyOrder(TEST_TEAM1_NAME, TEST_TEAM2_NAME);
        assertThat(result).extracting(TeamSummary::abbreviation).containsExactlyInAnyOrder(TEST_TEAM1_ABBREVIATION, TEST_TEAM2_ABBREVIATION);
    }

    @Test
    @DisplayName("Should successfully add a team as favourite")
    void testAddFavTeam() {
        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        when(this.teamRepository.findByName(TEST_TEAM3_NAME)).thenReturn(Optional.of(this.teams.get(2)));

        this.userService.addFavTeam(USER1_USERNAME, TEST_TEAM3_NAME);
        assertThat(this.user1.getFavTeams()).contains(this.teams.get(2));
        assertThat(this.teams.get(2).getFavoritedByUsers()).contains(this.user1);
        verify(this.userRepository).save(this.user1);
    }

    @Test
    @DisplayName("Should throw TeamAlreadyExistsException if the team is already marked as favourite")
    void testInvalidAddFavTeam() {
        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.teams.getFirst()));

        assertThatThrownBy(() -> this.userService.addFavTeam(USER1_USERNAME, TEST_TEAM1_NAME))
                .isInstanceOf(TeamAlreadyExistsException.class)
                .hasMessageContaining("Team Already Exists");
    }

    @Test
    @DisplayName("Should successfully remove a team as favourite")
    void testRemoveFavTeam() {
        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        when(this.teamRepository.findByName(TEST_TEAM2_NAME)).thenReturn(Optional.of(this.teams.get(1)));

        this.userService.removeFavTeam(USER1_USERNAME, TEST_TEAM2_NAME);
        assertThat(this.user1.getFavTeams()).doesNotContain(this.teams.get(1));
        assertThat(this.teams.get(1).getFavoritedByUsers()).doesNotContain(this.user1);
        verify(this.userRepository).save(this.user1);
    }

    @Test
    @DisplayName("Should throw TeamNotFoundException if the team to remove is not marked as favourite")
    void testInvalidRemoveFavTeam() {
        when(this.userRepository.findByUsername(USER1_USERNAME)).thenReturn(Optional.of(this.user1));
        when(this.teamRepository.findByName(TEST_TEAM3_NAME)).thenReturn(Optional.of(this.teams.get(2)));

        assertThatThrownBy(() -> this.userService.removeFavTeam(USER1_USERNAME, TEST_TEAM3_NAME))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining("Team Not Found");
    }
}