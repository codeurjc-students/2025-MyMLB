package com.mlb.mlbportal.unit;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.mlb.mlbportal.Models.UserEntity;
import com.mlb.mlbportal.Repositories.UserRepository;
import com.mlb.mlbportal.Services.UserService;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final UserEntity user1 = new UserEntity("fonssi@gmail.com", "fonssi29");
    private final UserEntity user2 = new UserEntity("armin@gmail.com", "armiin13");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.userRepository.save(user1);
        this.userRepository.save(user2);
    }

    @AfterEach
    public void tearDown() {
        this.userRepository.deleteAll();
    }

    @Test
    @DisplayName("getAllUsers should return all users from the repository")
    public void testGetAllUsers() {
        List<UserEntity> mockUsers = Arrays.asList(user1, user2);
        when(this.userRepository.findAll()).thenReturn(mockUsers);

        List<UserEntity> result = this.userService.getAllUsers();

        assertThat(result).hasSize(2);
        
        assertThat(result).extracting(UserEntity::getUsername)
            .containsExactly("fonssi29", "armiin13");
        
        assertThat(result).extracting(UserEntity::getEmail)
            .contains("fonssi@gmail.com", "armin@gmail.com");

        verify(this.userRepository, times(1)).findAll();
    }
}