package com.mlb.mlbportal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.handler.UserAlreadyExistsException;
import com.mlb.mlbportal.mappers.AuthenticationMapper;
import com.mlb.mlbportal.mappers.UserMapper;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final AuthenticationMapper authenticationMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepo, AuthenticationMapper authenticationMapper, UserMapper userMapper, PasswordEncoder password, EmailService emailService) {
        this.userRepository = userRepo;
        this.authenticationMapper = authenticationMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = password;
        this.emailService = emailService;
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

    public boolean resetPassword(String code, String newPassword) {
        Optional<PasswordResetToken> optReset = this.emailService.getCode(code);

        if (optReset.isEmpty()) {
            return false;
        }
        
        PasswordResetToken passwordReset = optReset.get();
        UserEntity user = passwordReset.getUser();
        if (user == null) {
            this.emailService.deleteToken(passwordReset);
            return false;
        }
        
        if (passwordReset.getExpirationDate().isBefore(LocalDateTime.now())) {
            passwordReset.getUser().setResetToken(null);
            this.emailService.deleteToken(passwordReset);
            return false;
        }

        user.setPassword(this.passwordEncoder.encode(newPassword));
        this.userRepository.save(user);

        user.setResetToken(null);
        this.emailService.deleteToken(passwordReset);
        return true;
    }
}