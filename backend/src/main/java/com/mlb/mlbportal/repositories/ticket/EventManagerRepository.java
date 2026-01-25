package com.mlb.mlbportal.repositories.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.ticket.EventManager;

import jakarta.persistence.LockModeType;

@Repository
public interface EventManagerRepository extends JpaRepository<EventManager, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT em FROM EventManager em WHERE em.id = :id")
    Optional<EventManager> findByIdWithBlock(@Param("id")Long id);

    @Query("SELECT em FROM EventManager em JOIN FETCH em.sector WHERE em.event.id = :id AND em.availability > 0")
    List<EventManager> findAvailableSectors(@Param("id")Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE EventManager em SET em.availability = em.availability - :ticketAmount
            WHERE em.id = :eventManagerId AND em.availability >= :ticketAmount
    """)
    int updateStockAvailability(@Param("eventManagerId")Long eventManagerId, @Param("ticketAmount")int ticketAmount);
}