package com.mlb.mlbportal.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mlb.mlbportal.dto.user.EditProfileRequest;
import com.mlb.mlbportal.dto.user.ProfileDTO;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.services.uploader.PictureService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.dto.user.UserRole;
import com.mlb.mlbportal.handler.conflict.TeamAlreadyExistsException;
import com.mlb.mlbportal.handler.conflict.UserAlreadyExistsException;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.handler.notFound.UserNotFoundException;
import com.mlb.mlbportal.mappers.AuthenticationMapper;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.mappers.UserMapper;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final AuthenticationMapper authenticationMapper;
    private final UserMapper userMapper;
    private final TeamMapper teamMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TeamRepository teamRepository;
    private final PaginationHandlerService paginationHandlerService;
    private final PictureService pictureService;

    @Transactional(readOnly = true)
    public Page<ShowUser> getAllUsers(int page, int size) {
        List<UserEntity> users = this.userRepository.findAll();
        return this.paginationHandlerService.paginateAndMap(users, page, size, this.userMapper::toShowUser);
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(String username) {
        return this.userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }

    private boolean existsUser(RegisterRequest registerRequest) {
        boolean usernameValidation = this.userRepository.findByUsername(registerRequest.username()).isPresent();
        boolean emailValidation = this.userRepository.findByEmail(registerRequest.email()).isPresent();
        return usernameValidation || emailValidation; 
    }

    @Transactional
    public RegisterRequest createUser(RegisterRequest registerRequest) {
        if (this.existsUser(registerRequest)) {
            throw new UserAlreadyExistsException();
        }
        String encodedPassword = this.passwordEncoder.encode(registerRequest.password());
        UserEntity newUser = new UserEntity(registerRequest.email(), registerRequest.username(), encodedPassword);
        newUser.getRoles().add("USER");
        this.userRepository.save(newUser);
        return this.authenticationMapper.toRegisterRequest(newUser);
    }

    @Transactional
    public void deleteAccount(String username) {
        UserEntity user = this.getUser(username);
        this.userRepository.delete(user);
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public UserRole getUserRole(String username) {
        UserEntity user = this.getUser(username);
        return this.authenticationMapper.toUserRole(user);
    }

    @Transactional(readOnly = true)
    public Set<TeamSummary> getFavTeamsOfAUser(String username) {
        UserEntity user = this.getUser(username);
        return this.teamMapper.toTeamSummarySet(user.getFavTeams());
    }

    @Transactional
    public void addFavTeam(String username, String teamName) {
        UserEntity user = this.getUser(username);
        Team team = this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
        if (!user.getFavTeams().add(team)) {
            throw new TeamAlreadyExistsException();
        }
        this.userRepository.save(user);
        team.getFavoritedByUsers().add(user);
    }

    @Transactional
    public void removeFavTeam(String username, String teamName) {
        UserEntity user = this.getUser(username);
        Team team = this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
        if (!user.getFavTeams().contains(team)) {
            throw new TeamNotFoundException();
        }
        user.getFavTeams().remove(team);
        this.userRepository.save(user);
        team.getFavoritedByUsers().remove(user);
    }

    @Transactional
    public PictureInfo changeProfilePicture(String username, MultipartFile file) throws IOException {
        UserEntity user = this.getUser(username);
        PictureInfo picture = this.pictureService.uploadPicture(file);
        user.setPicture(picture);
        this.userRepository.save(user);
        return picture;
    }

    @Transactional
    public void deleteProfilePicture(String username) {
        UserEntity user = this.getUser(username);
        user.setPicture(null);
        this.userRepository.save(user);
    }

    @Transactional
    public ShowUser updateProfile(String username, EditProfileRequest request) {
        UserEntity user = this.getUser(username);
        if (request.email() != null && !request.email().isEmpty()) {
            user.setEmail(request.email());
        }
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(this.passwordEncoder.encode(request.password()));
        }
        this.userRepository.save(user);
        return this.userMapper.toShowUser(user);
    }

    @Transactional(readOnly = true)
    public ProfileDTO getUserProfile(String username) {
        UserEntity user = this.getUser(username);
        return this.userMapper.toProfileDTO(user);
    }
}