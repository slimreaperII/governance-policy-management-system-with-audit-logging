package com.ms_service.governanceservice.service;

import com.ms_service.governanceservice.dto.PolicyEvent;
import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.grpc.AuditLogRequest;
import com.ms_service.governanceservice.grpc.AuditLogResponseList;
import com.ms_service.governanceservice.grpc.AuditLogServiceGrpc;
import com.ms_service.governanceservice.kafka.KafkaProducer;
import com.ms_service.governanceservice.policy.Policy;
import com.ms_service.governanceservice.policy.Status;
import com.ms_service.governanceservice.repository.PolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PolicyService {
    private final PolicyRepository repository;
    private final KafkaProducer kafkaProducer;
    private final AuditLogServiceGrpc.AuditLogServiceBlockingStub auditLogServiceBlockingStub;

    public PolicyService(PolicyRepository repository, KafkaProducer kafkaProducer, AuditLogServiceGrpc.AuditLogServiceBlockingStub auditLogServiceBlockingStub) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
        this.auditLogServiceBlockingStub = auditLogServiceBlockingStub;
    }

    @Transactional
    public PolicyResponse createPolicy (PolicyRequest request){
        boolean duplicate = repository.existsByTitleAndCreatedByAndDescription(
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

        Policy savedPolicy = repository.save(policy);

        kafkaProducer.sendPolicyEvent(
                new PolicyEvent(
                        "policy-created",
                        savedPolicy.getPolicyId(),
                        savedPolicy.getCreatedBy(),
                        LocalDateTime.now()
                )
        );

        return PolicyResponse.from(savedPolicy);
    }

    public List<PolicyResponse> getAllPolicy(){
        return repository.findAll().stream().map(PolicyResponse::from).toList();
    }

    public PolicyResponse getPolicyByID(Integer id) {
        return repository.findById(id).map(PolicyResponse::from).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found."));
    }

    public List<PolicyResponse> searchPolicies (String title, String creator, Status status) {
        title = (title == null) ? "" : title;
        creator = (creator == null) ? "" : creator;

        List<Policy> policies;

        if (status == null) {
            policies = repository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title, creator);
        } else {
            policies = repository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCaseAndStatus(title, creator, status);
        }

        return policies.stream().map(PolicyResponse::from).toList();
    }

    @Transactional
    public PolicyResponse submitPolicy (Integer id){
        Policy policy = repository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.DRAFT){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only policies in DRAFT can be submitted");
        }
        policy.setStatus(Status.PENDING_APPROVAL);

        Policy savedPolicy = repository.save(policy);

        kafkaProducer.sendPolicyEvent(
                new PolicyEvent(
                        "policy-submitted",
                        savedPolicy.getPolicyId(),
                        savedPolicy.getCreatedBy(),
                        LocalDateTime.now()
                )
        );

        return PolicyResponse.from(savedPolicy);
    }

    @Transactional
    public String deletePolicy (Integer id) {
        Policy policy = repository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.DRAFT){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only policies in DRAFT can be deleted");
        }
        repository.delete(policy);

        kafkaProducer.sendPolicyEvent(
                new PolicyEvent(
                        "policy-deleted",
                        policy.getPolicyId(),
                        policy.getCreatedBy(),
                        LocalDateTime.now()
                )
        );

        return "Policy deleted successfully";
    }

    @Transactional
    public PolicyResponse approvePolicy (Integer id){
        Policy policy = repository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.PENDING_APPROVAL){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only policies in PENDING_APPROVAL can be approved");
        }
        policy.setStatus(Status.ACCEPTED);

        Policy savedPolicy = repository.save(policy);

        kafkaProducer.sendPolicyEvent(
                new PolicyEvent(
                        "policy-approved",
                        savedPolicy.getPolicyId(),
                        savedPolicy.getCreatedBy(),
                        LocalDateTime.now()
                )
        );

        return PolicyResponse.from(savedPolicy);
    }

    @Transactional
    public PolicyResponse rejectPolicy (Integer id){
        Policy policy = repository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy Not Found"));
        if (policy.getStatus() != Status.PENDING_APPROVAL){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only policies in PENDING_APPROVAL can be rejected");
        }
        policy.setStatus(Status.REJECTED);

        Policy savedPolicy = repository.save(policy);

        kafkaProducer.sendPolicyEvent(
                new PolicyEvent(
                        "policy-rejected",
                        savedPolicy.getPolicyId(),
                        savedPolicy.getCreatedBy(),
                        LocalDateTime.now()
                )
        );

        return PolicyResponse.from(savedPolicy);
    }

    public List<PolicyEvent> getAllAuditLogsByPolicyId (Integer id) {
        AuditLogRequest request = AuditLogRequest.newBuilder().setPolicyId(id).build();
        AuditLogResponseList auditLogResponseList = auditLogServiceBlockingStub.getAllPolicyLog(request);

        return auditLogResponseList.getAuditLogResponseList().stream().map(
                log -> new PolicyEvent(
                        log.getEventType(),
                        log.getPolicyId(),
                        log.getActor(),
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(
                        log.getTimestamp().getSeconds(),
                        log.getTimestamp().getNanos()
                        ), java.time.ZoneId.systemDefault()
                        )
                )).toList();
    }

}
