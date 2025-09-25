package com.mlb.mlbportal.controllers;

import org.springframework.stereotype.Controller;

import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;

import jakarta.annotation.PostConstruct;

@Controller
public class InitController {
    
    private final UserRepository userRepository;

    public InitController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        this.userRepository.save(new UserEntity("fonssi@gmail.com", "fonssi29"));
        this.userRepository.save(new UserEntity("armin@gmail.com", "armiin13"));
    }
}