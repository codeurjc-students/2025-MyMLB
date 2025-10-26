package com.mlb.mlbportal.e2e;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;
import static com.mlb.mlbportal.utils.TestConstants.LOGOUT_PATH;
import static com.mlb.mlbportal.utils.TestConstants.ME_PATH;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTestAux {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setupTestData() {
        this.userRepository.deleteAll();

        UserEntity user = new UserEntity();
        user.setUsername(TEST_USER_USERNAME);
        user.setPassword(passwordEncoder.encode(TEST_USER_PASSWORD));
        user.setRoles(List.of("USER"));
        this.userRepository.save(user);
    }

    @Test
    @WithMockUser(username = TEST_USER_USERNAME, roles = { "USER" })
    @DisplayName("GET /api/auth/me should successfully return the current authenticated user")
    void testGetActiveUser() throws Exception {
        this.mockMvc.perform(get(ME_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USER_USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    @DisplayName("GET /api/auth/me with a non authenticated user should return 401")
    void testGetActiveUserWithoutAuthentication() throws Exception {
        this.mockMvc.perform(get(ME_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEST_USER_USERNAME)
    @DisplayName("POST /api/auth/logout should return success response")
    void testLogoutWithAuthenticatedUser() throws Exception {
        this.mockMvc.perform(post(LOGOUT_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Logout Successful"));
    }

    @Test
    @DisplayName("POST /api/auth/logout with a non authenticated user should return a 400")
    void testLogoutWithoutAuthentication() throws Exception {
        this.mockMvc.perform(post(LOGOUT_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("There is no user to logout"));
    }
}