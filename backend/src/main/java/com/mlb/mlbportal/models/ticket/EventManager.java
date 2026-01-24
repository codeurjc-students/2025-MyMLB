package com.mlb.mlbportal.models.ticket;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_EVENT_MANAGER")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventManager {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    private double price;

    private int availability;

    public EventManager(Event event, Sector sector, double price) {
        this.event = event;
        this.sector = sector;
        this.price = price;
        this.availability = sector.getTotalCapacity();
    }
}