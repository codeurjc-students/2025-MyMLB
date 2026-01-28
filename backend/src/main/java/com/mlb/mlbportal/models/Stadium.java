package com.mlb.mlbportal.models;

import java.util.ArrayList;
import java.util.List;

import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.models.ticket.Sector;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(name = "T_STADIUM")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private int openingDate;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<PictureInfo> pictures = new ArrayList<>();

    private PictureInfo pictureMap;

    @OneToOne(mappedBy = "stadium")
    private Team team;

    @OneToMany(mappedBy = "stadium", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Match> matches = new ArrayList<>();

    @OneToMany(mappedBy = "stadium", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sector> sectors = new ArrayList<>();

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

    public void addSector(Sector sector) {
        this.sectors.add(sector);
        sector.setStadium(this);
    }
}