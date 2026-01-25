package com.mlb.mlbportal.models.ticket;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_EVENT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @OneToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EventManager> eventManagers = new ArrayList<>();

    public Event(Stadium stadium, Match match) {
        this.stadium = stadium;
        this.match = match;
    }

    public void addEventManager(EventManager eventManager) {
        this.eventManagers.add(eventManager);
        eventManager.setEvent(this);
    }
}