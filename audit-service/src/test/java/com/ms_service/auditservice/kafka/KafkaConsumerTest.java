//package com.ms_service.auditservice.kafka;
//
//import com.ms_service.auditservice.dto.AuditLogEvent;
//import com.ms_service.auditservice.model.AuditLog;
//import com.ms_service.auditservice.repository.AuditLogRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class KafkaConsumerTest {
//    @Mock
//    private AuditLogRepository auditLogRepository;
//    @InjectMocks
//    private KafkaConsumer kafkaConsumer;
//
//    @Test
//    void shouldSaveConsumedEventSuccessfullyToDatabase () {
//        //given
//        AuditLogEvent event = new AuditLogEvent();
//        event.setEventType("policy-created");
//        event.setPolicyId(1);
//        event.setActor("admin");
//        event.setTimestamp(LocalDateTime.now());
//
//        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
//
//        //when
//        kafkaConsumer.consume(event);
//
//        //then
//        verify(auditLogRepository, times(1)).save(logCaptor.capture());
//        AuditLog log = logCaptor.getValue();
//        assertEquals(event.getEventType(), log.getEventType());
//        assertEquals(event.getPolicyId(), log.getPolicyId());
//        assertEquals(event.getActor(), log.getActor());
//    }
//
//}