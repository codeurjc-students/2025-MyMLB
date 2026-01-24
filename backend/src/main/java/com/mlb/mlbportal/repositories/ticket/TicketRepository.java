package com.mlb.mlbportal.repositories.ticket;

import com.mlb.mlbportal.models.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.eventManager em " +
            "JOIN FETCH em.event e " +
            "JOIN FETCH e.match " +
            "WHERE e.id = :eventId")
    List<Ticket> findTicketByEvent(@Param("eventId")Long eventId);
}