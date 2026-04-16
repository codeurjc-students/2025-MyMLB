package com.mlb.mlbportal.repositories;

import com.mlb.mlbportal.models.support.SupportMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    @EntityGraph(attributePaths = {"supportTicket"})
    List<SupportMessage> findBySupportTicketIdOrderByCreationDateAsc(Long ticketId);
}