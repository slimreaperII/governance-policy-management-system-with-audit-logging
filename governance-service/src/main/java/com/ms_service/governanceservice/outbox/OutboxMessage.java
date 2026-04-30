package com.ms_service.governanceservice.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox_table")
public class OutboxMessage {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "saga_id", nullable = false, updatable = false)
    private UUID sagaId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxMessageType type;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "saga_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;

    @Column(name = "outbox_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;
}
