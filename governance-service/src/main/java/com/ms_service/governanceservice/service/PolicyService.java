package com.ms_service.governanceservice.service;

import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.policy.Policy;
import com.ms_service.governanceservice.policy.Status;
import com.ms_service.governanceservice.repository.PolicyRepository;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {
    private final PolicyRepository repository;

    public PolicyService(PolicyRepository repository) {
        this.repository = repository;
    }

    public PolicyResponse createPolicy (PolicyRequest request){
        Policy policy = new Policy();
        policy.setTitle(request.getTitle());
        policy.setDescription(request.getDescription());
        policy.setCreatedBy(request.getCreatedBy());
        policy.setStatus(Status.DRAFT);

        repository.save(policy);

        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getTitle(),
                policy.getDescription(),
                policy.getStatus(),
                policy.getCreatedBy(),
                policy.getCreatedAt()
        );
    }
}
