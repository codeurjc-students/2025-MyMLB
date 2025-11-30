package com.mlb.mlbportal.models.player;

import com.mlb.mlbportal.models.Team;

import com.mlb.mlbportal.models.others.PictureInfo;
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

    private int playerNumber;
    
    @ManyToOne
    private Team team;

    private PictureInfo picture;

    protected Player(String name, int number, Team team) {
        this.name = name;
        this.playerNumber = number;
        this.team = team;
    }

    protected Player(String name, int number, Team team, PictureInfo picture) {
        this.name = name;
        this.playerNumber = number;
        this.team = team;
        this.picture = picture;
    }
}