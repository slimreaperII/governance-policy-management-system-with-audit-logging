package com.ms_service.auditservice.repository;

import com.ms_service.auditservice.outbox.OutboxMessage;
import com.ms_service.auditservice.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage,UUID> {
    List<OutboxMessage> findAllByOutboxStatusOrderByCreatedAtAsc (OutboxStatus status);
}
