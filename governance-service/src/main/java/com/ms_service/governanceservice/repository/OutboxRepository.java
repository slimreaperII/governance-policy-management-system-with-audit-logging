package com.ms_service.governanceservice.repository;

import com.ms_service.governanceservice.outbox.OutboxMessage;
import com.ms_service.governanceservice.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage,UUID> {
    List<OutboxMessage> findAllByOutboxStatusOrderByCreatedAtAsc (OutboxStatus status);
}
