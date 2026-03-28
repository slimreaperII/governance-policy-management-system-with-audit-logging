package com.ms_service.governanceservice.repository;

import com.ms_service.governanceservice.policy.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    boolean existsByTitleAndCreatedByAndDescription(String title, String createdBy, String description);
}
