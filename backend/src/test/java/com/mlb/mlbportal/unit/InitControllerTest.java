package com.mlb.mlbportal.unit;

import com.mlb.mlbportal.controllers.InitController;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

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

    @InjectMocks
    private InitController initController;

    private static final int TEAM_COUNT = 30;
    private static final int STADIUM_COUNT = 30;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() {
        ReflectionTestUtils.setField(this.initController, "fonssiPassword", "test1234");
        ReflectionTestUtils.setField(this.initController, "arminPassword", "test5678");
    }

    private List<Team> generateMockTeams(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Team t = new Team();
                    ReflectionTestUtils.setField(t, "name", "Team " + i);
                    return t;
                })
                .toList();
    }

    @Test
    void testCreateAdmins() {
        when(this.passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        ReflectionTestUtils.invokeMethod(this.initController, "createAdmins");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor
                .forClass(UserEntity.class);

        verify(this.userRepository, times(2)).save(userCaptor.capture());

        List<UserEntity> savedUsers = userCaptor.getAllValues();

        assertThat(savedUsers).hasSize(2);
        assertThat(savedUsers.get(0).getRoles()).contains("ADMIN");
        assertThat(savedUsers.get(1).getRoles()).contains("ADMIN");
        assertThat(savedUsers.get(0).getPassword()).isEqualTo("hashed_password");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSetUpTeams() {
        ReflectionTestUtils.invokeMethod(this.initController, "setUpTeams");

        ArgumentCaptor<List<Team>> teamListCaptor = ArgumentCaptor.forClass(List.class);

        verify(this.teamRepository, times(1)).saveAll(teamListCaptor.capture());

        assertThat(teamListCaptor.getValue()).hasSize(TEAM_COUNT);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSetUpStadiums() {
        when(this.teamRepository.findAll()).thenReturn(this.generateMockTeams(TEAM_COUNT));

        ReflectionTestUtils.invokeMethod(this.initController, "setUpStadiums");

        ArgumentCaptor<List<Stadium>> stadiumListCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.stadiumRepository, times(1)).saveAll(stadiumListCaptor.capture());
        assertThat(stadiumListCaptor.getValue()).hasSize(STADIUM_COUNT);

        verify(this.teamRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testSetUpMatches() {
        when(this.teamRepository.findAll()).thenReturn(this.generateMockTeams(TEAM_COUNT));

        ReflectionTestUtils.invokeMethod(this.initController, "setUpMatches");

        verify(this.matchRepository, times(1)).saveAll(anyList());

        verify(this.teamRepository, times(1)).saveAll(anyList());
    }
}