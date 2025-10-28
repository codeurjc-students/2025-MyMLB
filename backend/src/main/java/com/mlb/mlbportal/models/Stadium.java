package com.mlb.mlbportal.models;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

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

    private Year openingDate;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> pictures = new ArrayList<>();

    @OneToOne(mappedBy = "stadium")
    private Team team;

    @PreRemove
    public void preRemove() {
        if (team != null) {
            team.setStadium(null);
        }
    }

    public Stadium(String name, Year openingDate, List<String> pictures, Team team) {
        this.name = name;
        this.openingDate = openingDate;
        this.pictures = pictures;
        this.team = team;
    }

    public Stadium(String name, Year openingDate, Team team) {
        this.name = name;
        this.openingDate = openingDate;
        this.team = team;
    }
}