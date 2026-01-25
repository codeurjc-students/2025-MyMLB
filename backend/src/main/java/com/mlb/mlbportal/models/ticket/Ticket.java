package com.mlb.mlbportal.models.ticket;

import com.mlb.mlbportal.models.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_TICKET")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_manager_id")
    private EventManager eventManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity owner;

    private String ownerName;

    @OneToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private LocalDateTime purchaseDate;
}