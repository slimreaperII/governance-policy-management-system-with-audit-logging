package com.ms_service.auditservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms_service.auditservice.dto.AuditLogEvent;
import com.ms_service.auditservice.dto.AuditResult;
import com.ms_service.auditservice.dto.ResultStatus;
import com.ms_service.auditservice.model.AuditLog;
import com.ms_service.auditservice.outbox.OutboxMessage;
import com.ms_service.auditservice.outbox.OutboxMessageType;
import com.ms_service.auditservice.outbox.OutboxStatus;
import com.ms_service.auditservice.repository.AuditLogRepository;
import com.ms_service.auditservice.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
public class KafkaConsumer {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public KafkaConsumer(AuditLogRepository auditLogRepository, OutboxRepository outboxRepository,ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }

    @KafkaListener(topics = "governance-events", groupId = "audit_service")
    @Transactional
    public void consume (String payload){

        AuditLogEvent event;
        try {
            event = objectMapper.readValue(payload, AuditLogEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (
                !auditLogRepository.existsByEventTypeAndPolicyId(event.getEventType(), event.getPolicyId())
        ){
            AuditLog log = new AuditLog();
            log.setEventType(event.getEventType());
            log.setPolicyId(event.getPolicyId());
            log.setActor(event.getActor());
            log.setTimeStamp(event.getTimestamp());

            auditLogRepository.save(log);

            AuditResult auditResult = new AuditResult();
            auditResult.setSagaId(event.getSagaId());
            auditResult.setAuditResult(ResultStatus.SUCCESSFUL_AUDIT);

            String resultPayload;
            try {
                resultPayload = objectMapper.writeValueAsString(auditResult);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize event", e);
            }

            OutboxMessage result = new OutboxMessage();
            result.setSagaId(event.getSagaId());
            result.setType(OutboxMessageType.SUCCESSFUL_AUDIT);
            result.setCreatedAt(Instant.now());
            result.setPayload(resultPayload);
            result.setOutboxStatus(OutboxStatus.STARTED);

            outboxRepository.save(result);
        }
    }
}
