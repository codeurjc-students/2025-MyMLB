package com.mlb.mlbportal.repositories;

import com.mlb.mlbportal.models.support.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findBySupportTicketIdOrderByCreationDateAsc(Long ticketId);
}