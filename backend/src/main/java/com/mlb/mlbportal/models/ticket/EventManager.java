package com.mlb.mlbportal.models.ticket;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "T_Event_Manager")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @OneToMany(mappedBy = "eventManager", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Ticket> tickets = new LinkedList<>();

    private double price;

    private int availability;

    public EventManager(Event event, Sector sector, double price) {
        this.event = event;
        this.sector = sector;
        this.price = price;
        this.availability = sector.getTotalCapacity();
    }
}