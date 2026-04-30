//package com.ms_service.governanceservice.kafka;
//
//import com.ms_service.governanceservice.dto.PolicyEvent;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.messaging.Message;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class KafkaProducerTest {
//    @Mock
//    private  KafkaTemplate<String, PolicyEvent> kafkaTemplate;
//    @Mock
//    private  NewTopic eventTopic;
//
//    @InjectMocks
//    private KafkaProducer kafkaProducer;
//
//    @Test
//    void shouldSendPolicyEventSuccessfully() {
//        //given
//        PolicyEvent event = new PolicyEvent();
//        event.setEventType("policy-created");
//        event.setPolicyId(1);
//        event.setActor("admin");
//        event.setTimestamp(LocalDateTime.now());
//
//        when(eventTopic.name()).thenReturn("governance-events");
//        ArgumentCaptor<Message<PolicyEvent>> messageCaptor =
//                ArgumentCaptor.forClass(Message.class);
//
//        //when
//        kafkaProducer.sendPolicyEvent(event);
//
//        //then
//        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
//
//        Message<PolicyEvent> message = messageCaptor.getValue();
//        assertEquals("governance-events", message.getHeaders().get("kafka_topic"));
//        assertEquals(event, message.getPayload());
//    }
//}