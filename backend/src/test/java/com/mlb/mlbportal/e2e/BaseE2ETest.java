package com.mlb.mlbportal.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mlb.mlbportal.repositories.UserRepository;

import io.restassured.RestAssured;

import static com.mlb.mlbportal.utils.TestConstants.*;

public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    @SuppressWarnings("unused")
    void setupRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    protected void cleanDatabase() {
        this.userRepository.deleteAll();
    }

    protected void saveTestUser(String email, String username, String password) {
        this.userRepository.save(new com.mlb.mlbportal.models.UserEntity(email, username, passwordEncoder.encode(password)));
    }
}