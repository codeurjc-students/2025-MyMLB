package com.mlb.mlbportal.controllers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;

import jakarta.annotation.PostConstruct;

@Controller
public class InitController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitController(UserRepository userRepository, PasswordEncoder passsEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passsEncoder;
    }

    @PostConstruct
    public void init() {
        UserEntity fonssiUser = new UserEntity("fonssitorodriguezgutt@gmail.com", "fonssi29", this.passwordEncoder.encode("123"));
        UserEntity arminUser = new UserEntity("armin@gmail.com", "armiin13", this.passwordEncoder.encode("345"));
        
        fonssiUser.getRoles().add("ADMIN");
        arminUser.getRoles().add("ADMIN");
        
        this.userRepository.save(fonssiUser);
        this.userRepository.save(arminUser);
    }
}