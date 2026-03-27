package com.ms_service.governanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Integer policyId;
    private String title;
    private String description;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
}
