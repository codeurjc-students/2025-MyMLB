package com.mlb.mlbportal.repositories.ticket;

import com.mlb.mlbportal.handler.notFound.EventNotFoundException;
import com.mlb.mlbportal.models.ticket.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    default Event findEventByIdOrElseThrow(Long eventId) {
        return this.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Query("SELECT e FROM Event e JOIN FETCH e.match m WHERE m.id = :matchId AND m.date > CURRENT_TIMESTAMP")
    Optional<Event> findEventByMatchId(@Param("matchId") Long matchId);
}