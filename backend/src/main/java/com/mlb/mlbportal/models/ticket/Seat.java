package com.mlb.mlbportal.models.ticket;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_SEAT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Column(name = "is_occupied")
    private boolean isOccupied = false;

    public Seat(String name, Sector sector, boolean state) {
        this.name = name;
        this.sector = sector;
        this.isOccupied = state;
    }
}