package com.mlb.mlbportal.models.player;

import com.mlb.mlbportal.models.Team;

import com.mlb.mlbportal.models.others.PictureInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_Player", indexes = {
        @Index(name = "idx_player_name", columnList = "name")
})
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private int statsApiId;

    private String name;

    private int playerNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private PictureInfo picture;

    private boolean apiDataSource = true;

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