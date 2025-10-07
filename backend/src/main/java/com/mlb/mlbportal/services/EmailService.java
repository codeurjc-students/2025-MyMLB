package com.mlb.mlbportal.services;

import java.util.Optional;
import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mlb.mlbportal.handler.UserNotFoundException;
import com.mlb.mlbportal.models.PasswordResetToken;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.repositories.PasswordResetTokenRepository;
import com.mlb.mlbportal.repositories.UserRepository;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordRepository;
    private final UserRepository userRepository;

    public EmailService(JavaMailSender javaMail, PasswordResetTokenRepository passRepo, UserRepository userRepo) {
        this.mailSender = javaMail;
        this.passwordRepository = passRepo;
        this.userRepository = userRepo;
    }

    public Optional<PasswordResetToken> getCode(String code) {
        return this.passwordRepository.findByCode(code);
    }

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

    public void sendEmail(String destinyEmail) {
        UserEntity user = this.userRepository.findByEmail(destinyEmail).orElseThrow(() -> new UserNotFoundException("There is no user registered with this email"));
        
        this.passwordRepository.findByUser(user).ifPresent(this.passwordRepository::delete);
        
        String code = String.format("%04d", new Random().nextInt(10000));
        
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
}