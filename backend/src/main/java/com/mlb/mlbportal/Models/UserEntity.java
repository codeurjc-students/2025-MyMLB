package com.mlb.mlbportal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String username;

    public UserEntity() {}

    public UserEntity(String email, String username) {
        this.email = email;
        this.username = username;
    }
}