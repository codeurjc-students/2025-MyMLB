package com.mlb.mlbportal.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.User.ShowUser;
import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.handler.UserAlreadyExistsException;
import com.mlb.mlbportal.mappers.AuthenticationMapper;
import com.mlb.mlbportal.mappers.UserMapper;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final AuthenticationMapper authenticationMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, AuthenticationMapper authenticationMapper, UserMapper userMapper, PasswordEncoder password) {
        this.userRepository = userRepo;
        this.authenticationMapper = authenticationMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = password;
    }

    public List<ShowUser> getAllUsers() {
        return this.userMapper.toShowUsers(this.userRepository.findAll());
    }

    public RegisterRequest createUser(RegisterRequest registerRequest) {
        if (this.userRepository.findByUsername(registerRequest.username()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        String encodedPassword = this.passwordEncoder.encode(registerRequest.password());
        UserEntity newUser = new UserEntity(registerRequest.email(), registerRequest.username(), encodedPassword);
        newUser.getRoles().add("USER");
        this.userRepository.save(newUser);
        return this.authenticationMapper.toRegisterRequest(newUser);
    }
}