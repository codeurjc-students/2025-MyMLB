package com.mlb.mlbportal.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mlb.mlbportal.Models.UserEntity;
import com.mlb.mlbportal.Repositories.UserRepository;
import com.mlb.mlbportal.Services.UserService;

@SpringBootTest
public class UserServiceIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final UserEntity user1 = new UserEntity("fonssi@gmail.com", "fonssi29");
    private final UserEntity user2 = new UserEntity("armin@gmail.com", "armiin13");

    @BeforeEach
    public void setUp() {
        this.userRepository.deleteAll();
        this.userRepository.save(user1);
        this.userRepository.save(user2);
    }

    @Test
    @DisplayName("getAllUsers should return all users from the database")
    public void getAllUsersTest() {
        List<UserEntity> result = this.userService.getAllUsers();

        assertThat(result).hasSize(2).extracting(UserEntity::getUsername)
            .containsExactlyInAnyOrder("fonssi29", "armiin13");
    }
}