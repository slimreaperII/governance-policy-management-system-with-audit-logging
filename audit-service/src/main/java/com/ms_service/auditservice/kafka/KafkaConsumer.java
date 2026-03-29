package com.ms_service.auditservice.kafka;

import com.ms_service.auditservice.dto.AuditLogEvent;
import com.ms_service.auditservice.model.AuditLog;
import com.ms_service.auditservice.repository.AuditLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private final AuditLogRepository auditLogRepository;

    public KafkaConsumer(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @KafkaListener(topics = "governance-events", groupId = "audit_service")
    public void consume (AuditLogEvent event){
        AuditLog log = new AuditLog();
        log.setEventType(event.getEventType());
        log.setPolicyId(event.getPolicyId());
        log.setActor(event.getActor());
        log.setTimeStamp(event.getTimestamp());

        auditLogRepository.save(log);
    }
}
