package com.mlb.mlbportal.models;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
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

    @OneToOne(mappedBy="user", cascade= CascadeType.ALL, orphanRemoval=true)
    private PasswordResetToken resetToken;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_fav_teams",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> favTeams = new HashSet<>();

    @ElementCollection(fetch= FetchType.EAGER)
    private List<String> roles = new LinkedList<>();

    @PreRemove
    public void preRemove() {
        for (Team team : favTeams) {
            team.getFavoritedByUsers().remove(this);
        }
        this.favTeams.clear();
    }

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