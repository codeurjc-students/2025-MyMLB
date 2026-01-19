package com.mlb.mlbportal.repositories;

import com.mlb.mlbportal.models.support.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    @Query("SELECT t FROM SupportTicket t WHERE t.status = com.mlb.mlbportal.models.enums.SupportTicketStatus.OPEN")
    List<SupportTicket> findAllOpenTickets();
}