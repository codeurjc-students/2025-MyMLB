package com.mlb.mlbportal.models.player;

import com.mlb.mlbportal.models.Team;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    
    @ManyToOne
    private Team team;

    private String picture;

    protected Player(String name, Team team) {
        this.name = name;
        this.team = team;
    }

    protected Player(String name, Team team, String picture) {
        this.name = name;
        this.team = team;
        this.picture = picture;
    }
}