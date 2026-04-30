package com.ms_service.governanceservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms_service.governanceservice.dto.PolicyEvent;
import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.grpc.AuditLogRequest;
import com.ms_service.governanceservice.grpc.AuditLogResponseList;
import com.ms_service.governanceservice.grpc.AuditLogServiceGrpc;
import com.ms_service.governanceservice.outbox.OutboxMessage;
import com.ms_service.governanceservice.outbox.OutboxMessageType;
import com.ms_service.governanceservice.outbox.OutboxStatus;
import com.ms_service.governanceservice.outbox.SagaStatus;
import com.ms_service.governanceservice.policy.Policy;
import com.ms_service.governanceservice.policy.Status;
import com.ms_service.governanceservice.repository.OutboxRepository;
import com.ms_service.governanceservice.repository.PolicyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final AuditLogServiceGrpc.AuditLogServiceBlockingStub auditLogServiceBlockingStub;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PolicyService(PolicyRepository policyRepository, AuditLogServiceGrpc.AuditLogServiceBlockingStub auditLogServiceBlockingStub, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.policyRepository = policyRepository;
        this.auditLogServiceBlockingStub = auditLogServiceBlockingStub;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PolicyResponse createPolicy (PolicyRequest request){
        boolean duplicate = policyRepository.existsByTitleAndCreatedByAndDescription(
                request.getTitle(),
                request.getCreatedBy(),
                request.getDescription()
        );
        if (duplicate){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Policy already exists");
        }

        Policy policy = new Policy();
        policy.setTitle(request.getTitle());
        policy.setDescription(request.getDescription());
        policy.setCreatedBy(request.getCreatedBy());
        policy.setStatus(Status.DRAFT);

        Policy savedPolicy = policyRepository.save(policy);

        UUID sagaId = UUID.randomUUID();

        PolicyEvent event =  new PolicyEvent(
                        sagaId,
                        "POLICY_CREATED",
                        savedPolicy.getPolicyId(),
                        savedPolicy.getCreatedBy(),
                        Instant.now()
                );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        OutboxMessage message = new OutboxMessage();
        message.setSagaId(sagaId);
        message.setCreatedAt(Instant.now());
        message.setPayload(payload);
        message.setType(OutboxMessageType.POLICY_CREATED);
        message.setOutboxStatus(OutboxStatus.STARTED);
        message.setSagaStatus(SagaStatus.STARTED);

        outboxRepository.save(message);
        return PolicyResponse.from(savedPolicy);
    }

    public List<PolicyResponse> getAllPolicy(){
        return policyRepository.findAll().stream().map(PolicyResponse::from).toList();
    }

    public PolicyResponse getPolicyByID(Integer id) {
        return policyRepository.findById(id).map(PolicyResponse::from).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found."));
    }

    public List<PolicyResponse> searchPolicies (String title, String creator, Status status) {
        title = (title == null) ? "" : title;
        creator = (creator == null) ? "" : creator;

        List<Policy> policies;

        if (status == null) {
            policies = policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title, creator);
        } else {
            policies = policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCaseAndStatus(title, creator, status);
        }

        return policies.stream().map(PolicyResponse::from).toList();
    }

    @Transactional
    public PolicyResponse submitPolicy (Integer id){
        Policy policy = policyRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.DRAFT){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Policies in DRAFT can be submitted");
        }
        policy.setStatus(Status.PENDING_APPROVAL);

        Policy savedPolicy = policyRepository.save(policy);

        UUID sagaId = UUID.randomUUID();

        PolicyEvent event =  new PolicyEvent(
                sagaId,
                "POLICY_SUBMITTED",
                savedPolicy.getPolicyId(),
                savedPolicy.getCreatedBy(),
                Instant.now()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        OutboxMessage message = new OutboxMessage();
        message.setSagaId(sagaId);
        message.setCreatedAt(Instant.now());
        message.setPayload(payload);
        message.setType(OutboxMessageType.POLICY_SUBMITTED);
        message.setOutboxStatus(OutboxStatus.STARTED);
        message.setSagaStatus(SagaStatus.STARTED);

        outboxRepository.save(message);
        return PolicyResponse.from(savedPolicy);
    }

    @Transactional
    public String deletePolicy (Integer id) {
        Policy policy = policyRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.DRAFT){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only policies in DRAFT can be deleted");
        }
        policyRepository.delete(policy);

        UUID sagaId = UUID.randomUUID();

        PolicyEvent event =  new PolicyEvent(
                sagaId,
                "POLICY_DELETED",
                policy.getPolicyId(),
                policy.getCreatedBy(),
                Instant.now()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        OutboxMessage message = new OutboxMessage();
        message.setSagaId(sagaId);
        message.setCreatedAt(Instant.now());
        message.setPayload(payload);
        message.setType(OutboxMessageType.POLICY_DELETED);
        message.setOutboxStatus(OutboxStatus.STARTED);
        message.setSagaStatus(SagaStatus.STARTED);

        outboxRepository.save(message);
        return "Policy deleted successfully";
    }

    @Transactional
    public PolicyResponse approvePolicy (Integer id){
        Policy policy = policyRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.PENDING_APPROVAL){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only policies in PENDING_APPROVAL can be approved");
        }
        policy.setStatus(Status.ACCEPTED);

        Policy savedPolicy = policyRepository.save(policy);

        UUID sagaId = UUID.randomUUID();

        PolicyEvent event =  new PolicyEvent(
                sagaId,
                "POLICY_APPROVED",
                savedPolicy.getPolicyId(),
                savedPolicy.getCreatedBy(),
                Instant.now()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        OutboxMessage message = new OutboxMessage();
        message.setSagaId(sagaId);
        message.setCreatedAt(Instant.now());
        message.setPayload(payload);
        message.setType(OutboxMessageType.POLICY_APPROVED);
        message.setOutboxStatus(OutboxStatus.STARTED);
        message.setSagaStatus(SagaStatus.STARTED);

        outboxRepository.save(message);
        return PolicyResponse.from(savedPolicy);
    }

    @Transactional
    public PolicyResponse rejectPolicy (Integer id){
        Policy policy = policyRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.PENDING_APPROVAL){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only policies in PENDING_APPROVAL can be rejected");
        }
        policy.setStatus(Status.REJECTED);

        Policy savedPolicy = policyRepository.save(policy);

        UUID sagaId = UUID.randomUUID();

        PolicyEvent event =  new PolicyEvent(
                sagaId,
                "POLICY_REJECTED",
                savedPolicy.getPolicyId(),
                savedPolicy.getCreatedBy(),
                Instant.now()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        OutboxMessage message = new OutboxMessage();
        message.setSagaId(sagaId);
        message.setCreatedAt(Instant.now());
        message.setPayload(payload);
        message.setType(OutboxMessageType.POLICY_REJECTED);
        message.setOutboxStatus(OutboxStatus.STARTED);
        message.setSagaStatus(SagaStatus.STARTED);

        outboxRepository.save(message);
        return PolicyResponse.from(savedPolicy);
    }

//    @CircuitBreaker( name = "governanceService", fallbackMethod = "AuditLogFallback")
//    public List<PolicyEvent> getAllAuditLogsByPolicyId (Integer id) {
//        AuditLogRequest request = AuditLogRequest.newBuilder().setPolicyId(id).build();
//        AuditLogResponseList auditLogResponseList = auditLogServiceBlockingStub.getAllPolicyLog(request);
//
//        return auditLogResponseList.getAuditLogResponseList().stream().map(
//                log -> new PolicyEvent(
//                        log.getEventType(),
//                        log.getPolicyId(),
//                        log.getActor(),
//                        Instant.ofEpochSecond(
//                                log.getTimestamp().getSeconds(),
//                                log.getTimestamp().getNanos()
//                        )
//                )).toList();
//    }
//
//    public List<PolicyEvent> AuditLogFallback (Integer id, Throwable e) {
//        return List.of(
//                new PolicyEvent(
//                        UUID.randomUUID(),
//                        "audit-log temporarily unavailable, please try again later.",
//                        id,
//                        "system",
//                        Instant.now()
//                )
//        );
//    }

}
