package com.mlb.mlbportal.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_Password_Reset_Token")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private String code;

    @OneToOne
    private UserEntity user;

    private LocalDateTime expirationDate;

    public PasswordResetToken(String code, UserEntity user) {
        this.code = code;
        this.user = user;
        this.expirationDate = LocalDateTime.now().plusMinutes(15);
    }
}