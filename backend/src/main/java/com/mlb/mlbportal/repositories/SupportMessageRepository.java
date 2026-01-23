package com.mlb.mlbportal.repositories;

import com.mlb.mlbportal.models.support.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {
    List<SupportMessage> findBySupportTicketIdOrderByCreationDateAsc(UUID ticketId);
}