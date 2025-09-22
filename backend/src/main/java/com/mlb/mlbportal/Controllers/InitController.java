package com.mlb.mlbportal.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.mlb.mlbportal.Models.UserEntity;
import com.mlb.mlbportal.Repositories.UserRepository;

import jakarta.annotation.PostConstruct;

@Controller
public class InitController {
    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        this.userRepository.save(new UserEntity("fonssi@gmail.com", "fonssi29"));
        this.userRepository.save(new UserEntity("armin@gmail.com", "armiin13"));
    }
}