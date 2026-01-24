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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @OneToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventManager> eventManagers = new ArrayList<>();

    public void addEventManager(EventManager eventManager) {
        this.eventManagers.add(eventManager);
        eventManager.setEvent(this);
    }
}