package com.mlb.mlbportal.repositories.ticket;

import com.mlb.mlbportal.models.ticket.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s JOIN EventManager em ON s.sector.id = em.sector.id " +
            "WHERE s.sector.id = :sectorId AND em.event.id = :eventId AND s.isOccupied = FALSE"
    )
    List<Seat> findAvailableSeats(@Param("sectorId")Long sectorId, @Param("eventId")Long eventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Seat s SET s.isOccupied = true WHERE s.id IN :seatIds AND s.isOccupied = false")
    int markSeatAsOccupied(@Param("seatIds")List<Long>seatIds);
}