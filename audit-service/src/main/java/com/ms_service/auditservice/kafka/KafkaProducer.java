package com.ms_service.auditservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NewTopic auditTopic;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate, NewTopic auditTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditTopic = auditTopic;
    }

    public void sendPolicyEvent(String payload) {
        Message<String> message = MessageBuilder.withPayload(payload).setHeader("kafka_topic", auditTopic.name()).build();
        kafkaTemplate.send(message);
    }
}
