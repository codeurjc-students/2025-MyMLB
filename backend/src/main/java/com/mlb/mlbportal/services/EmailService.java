package com.mlb.mlbportal.services;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.repositories.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.handler.notFound.UserNotFoundException;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.repositories.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService {
    private static final Random RANDOM = new Random();

    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    private EmailService self;

    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> getCode(String code) {
        return this.passwordRepository.findByCode(code);
    }

    @Transactional
    public void deleteToken(PasswordResetToken token) {
        this.passwordRepository.delete(token);
    }

    private void sendEmailHelper(String destinyEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinyEmail);
        message.setSubject(subject);
        message.setText(body);
        this.mailSender.send(message);
    }

    @Transactional
    public void sendEmail(String destinyEmail) {
        UserEntity user = this.userRepository.findByEmail(destinyEmail).orElseThrow(() -> new UserNotFoundException("There is no user registered with this email"));
        
        this.passwordRepository.findByUser(user).ifPresent(this.passwordRepository::delete);
        
        String code = String.format("%04d", RANDOM.nextInt(10000));
        
        PasswordResetToken resetToken = new PasswordResetToken(code, user);
        this.passwordRepository.save(resetToken);

        String subject = "Password Recovery";
        String body = """
                Hello %s,

                You have requested to generate a new password for your account.
                Please use the following 4-digit code to reset your password:

                %s

                This code will expire in 15 minutes.

                If you do not authorize this procedure, ignore this email.
                """.formatted(
                    user.getName() != null ? user.getName() : user.getUsername(),
                    code
                );
        this.sendEmailHelper(destinyEmail, subject, body);
    }

    public void sendProfileChangeNotification(String username, String oldEmail, String newEmail, boolean passwordChange) {
        String subject = "Profile Update Confirmation";

        String body = """
            Hello %s,
           
            We would like to inform you that %s
            
            If you did not authorize these modifications, please contact us immediately at mlbportal@gmail.com so we can investigate the issue and assist you accordingly.
            
            Thank you for your attention.
            
            Kind regards,
            
            The MLB Portal Team
    """.formatted(username, this.getKeyWords(newEmail, passwordChange));

        this.sendEmailHelper(oldEmail, subject, body);
    }

    private String getKeyWords(String newEmail, boolean passwordChange) {
        if (passwordChange) {
            return "your password have been successfully updated and your new credentials have been saved.";
        }
        else {
            return "your email have been successfully updated to " + newEmail + " and your credentials have been saved.";
        }
    }

    @Autowired
    public void setSelf(EmailService self) {
        this.self = self;
    }

    @Transactional(readOnly = true)
    public void sendDynamicGameReminder(Long matchId) {
        Match match = this.matchRepository.findById(matchId).orElse(null);
        if (match == null) return;

        Set<UserEntity> currentFans = new HashSet<>();

        if (match.getHomeTeam() != null) {
            currentFans.addAll(match.getHomeTeam().getFavoritedByUsers());
        }
        if (match.getAwayTeam() != null) {
            currentFans.addAll(match.getAwayTeam().getFavoritedByUsers());
        }

        Set<UserEntity> filteredUsers = currentFans.stream().filter(UserEntity::isEnableNotifications).collect(Collectors.toSet());

        if (!filteredUsers.isEmpty()) {
            this.self.sendGameReminder(filteredUsers, match);
        }
    }

    @Async
    protected void sendGameReminder(Set<UserEntity> users, Match match) {
        if (users == null || users.isEmpty()) {
            return;
        }

        String subject = "Match Starting Soon!";
        String body = """
        Hello,
        
        The match %s vs %s will begin in 10 minutes.
        
        Good luck to your favorite teams!
        """.formatted(match.getAwayTeam().getName(), match.getHomeTeam().getName());

        String[] emails = users.stream().map(UserEntity::getEmail).toArray(String[]::new);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setBcc(emails);
        message.setSubject(subject);
        message.setText(body);

        this.mailSender.send(message);
    }
}