package com.ms_service.governanceservice.service;

import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.policy.Policy;
import com.ms_service.governanceservice.policy.Status;
import com.ms_service.governanceservice.repository.PolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PolicyService {
    private final PolicyRepository repository;

    public PolicyService(PolicyRepository repository) {
        this.repository = repository;
    }

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

        return PolicyResponse.from(repository.save(policy));
    }

    public List<PolicyResponse> getAllPolicy(){
        return repository.findAll().stream().map(PolicyResponse::from).toList();
    }
}
