package com.mlb.mlbportal.repositories.ticket;

import com.mlb.mlbportal.models.ticket.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @EntityGraph(attributePaths = {"eventManager", "eventManager.event", "eventManager.event.match"})
    @Query("SELECT t FROM Ticket t WHERE t.eventManager.event.id = :eventId")
    Page<Ticket> findTicketByEvent(@Param("eventId")Long eventId, Pageable pageable);
}