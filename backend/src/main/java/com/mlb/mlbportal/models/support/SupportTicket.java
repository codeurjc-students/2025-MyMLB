package com.mlb.mlbportal.models.support;

import com.mlb.mlbportal.models.enums.SupportTicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String subject;

    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupportTicketStatus status = SupportTicketStatus.OPEN;

    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<SupportMessage> messages;

    @Version // Optimistick Locking. This manage the concurrent access to tickets.
    private Long version;
}