package com.mlb.mlbportal.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.Models.UserEntity;
import com.mlb.mlbportal.Repositories.UserRepository;

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