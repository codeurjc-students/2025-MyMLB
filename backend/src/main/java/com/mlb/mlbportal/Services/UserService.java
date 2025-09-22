package com.mlb.mlbportal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepo) {
        this.userRepository = userRepo;
    }

    public List<UserEntity> getAllUsers() {
        return this.userRepository.findAll();
    }
}