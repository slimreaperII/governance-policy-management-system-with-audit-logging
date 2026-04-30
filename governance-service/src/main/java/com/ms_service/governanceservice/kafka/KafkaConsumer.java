package com.ms_service.governanceservice.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms_service.governanceservice.dto.AuditResult;
import com.ms_service.governanceservice.dto.ResultStatus;
import com.ms_service.governanceservice.outbox.OutboxMessage;
import com.ms_service.governanceservice.outbox.SagaStatus;
import com.ms_service.governanceservice.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class KafkaConsumer {
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public KafkaConsumer(ObjectMapper objectMapper, OutboxRepository outboxRepository) {
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }

    @KafkaListener(topics = "audit-events", groupId = "audit_result_service")
    public void consume (String payload){

        AuditResult result;
        try {
            result = objectMapper.readValue(payload, AuditResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        OutboxMessage outboxMessage = outboxRepository.findBySagaId(result.getSagaId());

        if (result.getAuditResult() == ResultStatus.SUCCESSFUL_AUDIT) {
            log.info("Audit result: " + result.getAuditResult());
            outboxMessage.setProcessedAt(Instant.now());
            outboxMessage.setSagaStatus(SagaStatus.SUCCEEDED);

            outboxRepository.save(outboxMessage);
        }

    }
}

