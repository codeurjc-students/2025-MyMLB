package com.mlb.mlbportal.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private String code;

    @OneToOne
    private UserEntity user;

    private LocalDateTime expirationDate;

    public PasswordResetToken() {}

    public PasswordResetToken(String code, UserEntity user) {
        this.code = code;
        this.user = user;
        this.expirationDate = LocalDateTime.now().plusMinutes(15);
    }
}