package com.ms_service.governanceservice.dto;

import com.ms_service.governanceservice.policy.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Integer policyId;
    private String title;
    private String description;
    private Status status;
    private String createdBy;
    private LocalDateTime createdAt;
}
