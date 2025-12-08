package com.mlb.mlbportal.unit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.mlb.mlbportal.controllers.InitController;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.MlbImportService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class InitControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private MlbImportService mlbImportService;

    @InjectMocks
    private InitController initController;

    private static final int TEAM_COUNT = 3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() {
        ReflectionTestUtils.setField(this.initController, "fonssiPassword", "test1234");
        ReflectionTestUtils.setField(this.initController, "arminPassword", "test5678");
    }

    /**
     * Helper method to generate mock teams using the factory.
     * @return List of mock Team entities.
     */
    private List<Team> generateMockTeams() {
        return BuildMocksFactory.setUpTeamMocks();
    }

    @Test
    void testCreateAdmins() {
        when(this.passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        ReflectionTestUtils.invokeMethod(this.initController, "createAdmins");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        verify(this.userRepository, times(2)).save(userCaptor.capture());

        List<UserEntity> savedUsers = userCaptor.getAllValues();

        assertThat(savedUsers).hasSize(2);
        assertThat(savedUsers.get(0).getRoles()).contains("ADMIN");
        assertThat(savedUsers.get(1).getRoles()).contains("ADMIN");
        assertThat(savedUsers.get(0).getPassword()).isEqualTo("hashed_password");
    }

    @Test
    void testSetUpTeams() {
        ReflectionTestUtils.invokeMethod(this.initController, "setUpTeams");
        verify(this.teamRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testSetUpStadiums() {
        when(this.teamRepository.findAll()).thenReturn(this.generateMockTeams());

        ReflectionTestUtils.invokeMethod(this.initController, "setUpStadiums");

        verify(this.stadiumRepository, times(1)).saveAll(anyList());
        verify(this.teamRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testSetUpMatches() {
        List<Team> mockTeams = this.generateMockTeams();
        when(this.teamRepository.findAll()).thenReturn(mockTeams);

        ReflectionTestUtils.invokeMethod(this.initController, "setUpMatches");

        verify(this.matchRepository, times(1)).saveAll(anyList());

        verify(this.teamRepository, times(1)).saveAll(mockTeams);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSetUpPlayers() {
        List<Team> mockTeams = this.generateMockTeams();
        when(this.teamRepository.findAll()).thenReturn(mockTeams);

        ReflectionTestUtils.invokeMethod(this.initController, "setUpPlayers");

        verify(this.teamRepository, times(1)).findAll();
        verify(this.teamRepository, times(1)).saveAll(anyList());

        ArgumentCaptor<List<Team>> teamListCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.teamRepository).saveAll(teamListCaptor.capture());

        List<Team> savedTeams = teamListCaptor.getValue();
        assertThat(savedTeams).hasSize(TEAM_COUNT).isEqualTo(mockTeams);
    }
}