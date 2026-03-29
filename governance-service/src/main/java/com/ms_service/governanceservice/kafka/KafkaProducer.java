package com.ms_service.governanceservice.kafka;

import com.ms_service.governanceservice.dto.PolicyEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private final KafkaTemplate<String, PolicyEvent> kafkaTemplate;
    private final NewTopic eventTopic;

    public KafkaProducer(KafkaTemplate<String, PolicyEvent> kafkaTemplate, NewTopic eventTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventTopic = eventTopic;
    }

    public void sendPolicyEvent(PolicyEvent event) {
        Message<PolicyEvent> message = MessageBuilder.withPayload(event).setHeader("kafka_topic", eventTopic.name()).build();
        kafkaTemplate.send(message);
    }
}
