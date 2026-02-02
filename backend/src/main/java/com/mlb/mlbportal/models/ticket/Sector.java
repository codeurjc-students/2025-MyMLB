package com.mlb.mlbportal.models.ticket;

import com.mlb.mlbportal.models.Stadium;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "T_SECTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @OneToMany(mappedBy = "sector", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Seat> seats = new LinkedList<>();

    private int totalCapacity;

    public Sector(String name, int capacity, Stadium stadium) {
        this.name = name;
        this.totalCapacity = capacity;
        this.stadium = stadium;
    }

    public void addSeat(Seat seat) {
        this.seats.add(seat);
        seat.setSector(this);
    }
}