package com.ms_service.governanceservice.repository;

import com.ms_service.governanceservice.policy.Policy;
import com.ms_service.governanceservice.policy.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    boolean existsByTitleAndCreatedByAndDescription(String title, String createdBy, String description);
    List<Policy> findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase (String title, String createdBy);
    List<Policy> findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCaseAndStatus (String title, String createdBy, Status status);
}
