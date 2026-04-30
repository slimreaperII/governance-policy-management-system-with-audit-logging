package com.ms_service.governanceservice.outbox;

import com.ms_service.governanceservice.kafka.KafkaProducer;
import com.ms_service.governanceservice.repository.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final KafkaProducer kafkaProducer;

    public OutboxProcessor(OutboxRepository outboxRepository,
                           KafkaProducer kafkaProducer) {
        this.outboxRepository = outboxRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {

        List<OutboxMessage> messages =
                outboxRepository.findAllByOutboxStatusOrSagaStatusOrderByCreatedAtAsc(OutboxStatus.STARTED, SagaStatus.STARTED);

        for (OutboxMessage message : messages) {
            kafkaProducer.sendPolicyEvent(message.getPayload());
            message.setOutboxStatus(OutboxStatus.COMPLETED);
        }
    }
}
