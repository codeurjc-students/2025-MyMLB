package com.mlb.mlbportal.integration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static com.mlb.mlbportal.utils.TestConstants.NEW_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

import com.mlb.mlbportal.dto.user.EditProfileRequest;
import com.mlb.mlbportal.dto.user.ProfileDTO;
import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.handler.notFound.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.UserService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
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

    @MockitoBean
    @SuppressWarnings("unused")
    private EmailService emailService;

    @Autowired
    private TeamRepository teamRepository;

    private UserEntity user1;
    private UserEntity user2;

    private PasswordResetToken validToken;
    private PasswordResetToken invalidToken;

    private List<Team> teams;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.userRepository.deleteAll();
        this.teamRepository.deleteAll();

        this.user1 = new UserEntity(USER1_EMAIL, USER1_USERNAME, this.passwordEncoder.encode(USER1_PASSWORD));
        this.user2 = new UserEntity(USER2_EMAIL, USER2_USERNAME, this.passwordEncoder.encode(USER2_PASSWORD));

        this.userRepository.saveAll(List.of(this.user1, this.user2));

        this.validToken = new PasswordResetToken(VALID_CODE, this.user1);
        this.validToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        this.passwordRepository.save(this.validToken);

        this.invalidToken = new PasswordResetToken(INVALID_CODE, this.user2);
        this.invalidToken.setExpirationDate(LocalDateTime.now().minusMinutes(10));
        this.passwordRepository.save(this.invalidToken);

        this.teams = BuildMocksFactory.setUpTeamMocks();
        this.teamRepository.saveAll(this.teams);

        this.user1.setFavTeams(new HashSet<>(List.of(this.teams.get(0), this.teams.get(1))));
        this.userRepository.save(this.user1);
    }

    @Test
    @DisplayName("getAllUsers should return all users from the database")
    void testGetAllUsers() {
        Page<ShowUser> result = this.userService.getAllUsers(0, 10);

        assertThat(result).hasSize(2)
                .extracting(ShowUser::username)
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
    @DisplayName("Should delete the user's account successfully from the system")
    void testDeleteAccount() {
        this.userService.deleteAccount(USER1_USERNAME);

        assertThatThrownBy(() -> this.userService.getUser(USER1_USERNAME))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User Not Found");
    }

    @Test
    @DisplayName("Should edit the active user's profile correctly")
    void testEditProfile() {
        EditProfileRequest request = new EditProfileRequest(NEW_EMAIL, NEW_PASSWORD);
        this.userService.updateProfile(USER1_USERNAME, request);

        UserEntity storedUser = this.userRepository.findByUsername(USER1_USERNAME).orElseThrow();

        assertThat(storedUser.getEmail()).isEqualTo(NEW_EMAIL);
    }

    @Test
    @DisplayName("Should change the profile picture of the active user successfully")
    void testUpdateProfilePicture() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpg", "fake".getBytes());
        this.userService.changeProfilePicture(USER1_USERNAME, file);

        UserEntity storedUser = this.userRepository.findByUsername(USER1_USERNAME).orElseThrow();
        assertThat(storedUser.getPicture().getUrl()).contains("test.jpg");
    }

    @Test
    @DisplayName("Should delete the current profile picture of the user")
    void testDeleteProfilePicture() {
        this.userService.deleteProfilePicture(USER1_USERNAME);
        UserEntity storedUser = this.userRepository.findByUsername(USER1_USERNAME).orElseThrow();
        assertThat(storedUser.getPicture()).isNull();
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if the username does not exists")
    void testInvalidProfileDeletion() {
        assertThatThrownBy(() -> this.userService.deleteProfilePicture(UNKNOWN_USER))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User Not Found");
    }

    @Test
    @DisplayName("Should return correct favorite teams for a user")
    void testGetFavTeamsOfAUser() {
        Set<TeamSummary> result = this.userService.getFavTeamsOfAUser(USER1_USERNAME);

        assertThat(result).hasSize(2)
                .extracting(TeamSummary::name)
                .containsExactlyInAnyOrder(this.teams.get(0).getName(), this.teams.get(1).getName());
    }

    @Test
    @DisplayName("Should add a new team to user's favorites")
    void testAddFavTeam() {
        this.userService.addFavTeam(USER1_USERNAME, TEST_TEAM3_NAME);

        UserEntity updatedUser = this.userRepository.findByUsername(USER1_USERNAME).orElseThrow();
        assertThat(updatedUser.getFavTeams()).contains(this.teams.get(2));
        assertThat(this.teams.get(2).getFavoritedByUsers()).contains(updatedUser);
    }

    @Test
    @DisplayName("Should remove a team from user's favorites")
    void testRemoveFavTeam() {
        this.userService.removeFavTeam(USER1_USERNAME, TEST_TEAM1_NAME);
        UserEntity updatedUser = this.userRepository.findByUsername(USER1_USERNAME).orElseThrow();

        assertThat(updatedUser.getFavTeams()).doesNotContain(this.teams.getFirst());
        assertThat(this.teams.getFirst().getFavoritedByUsers()).doesNotContain(updatedUser);
    }
}