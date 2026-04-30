package com.ms_service.governanceservice.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms_service.governanceservice.dto.AuditResult;
import com.ms_service.governanceservice.dto.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {
    private final ObjectMapper objectMapper;

    public KafkaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "audit-events", groupId = "audit_result_service")
    public void consume (String payload){

        AuditResult result;
        try {
            result = objectMapper.readValue(payload, AuditResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (result.getAuditResult() == ResultStatus.SUCCESSFUL_AUDIT) {
            log.info("Audit result: " + result.getAuditResult());
        }

    }
}

