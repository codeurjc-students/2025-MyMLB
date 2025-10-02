package com.mlb.mlbportal.models;

import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String email;

    private String username;

    private String name;

    private String password;

    @ElementCollection(fetch= FetchType.EAGER)
    private List<String> roles = new LinkedList<>();

    public UserEntity() {
        this.roles.add("GUEST");  
    }

    public UserEntity(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public UserEntity(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public UserEntity(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }
}