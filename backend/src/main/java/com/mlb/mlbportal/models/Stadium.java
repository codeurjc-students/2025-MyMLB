package com.mlb.mlbportal.models;

import java.util.ArrayList;
import java.util.List;

import com.mlb.mlbportal.models.others.PictureInfo;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Setter
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private int openingDate;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<PictureInfo> pictures = new ArrayList<>();

    @OneToOne(mappedBy = "stadium")
    private Team team;

    @PreRemove
    public void preRemove() {
        if (team != null) {
            team.setStadium(null);
        }
    }

    public Stadium(String name, int openingDate, List<PictureInfo> pictures, Team team) {
        this.name = name;
        this.openingDate = openingDate;
        this.pictures = pictures;
        this.team = team;
    }

    public Stadium(String name, int openingDate, Team team) {
        this.name = name;
        this.openingDate = openingDate;
        this.team = team;
    }
}