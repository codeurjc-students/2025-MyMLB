package com.mlb.mlbportal.models.support;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "support_ticket_id")
    @ToString.Exclude
    private SupportTicket supportTicket;

    private String senderEmail;

    private String body;

    @Builder.Default
    private boolean isFromUser = true;

    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();
}