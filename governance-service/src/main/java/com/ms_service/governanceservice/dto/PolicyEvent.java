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
public class PolicyEvent {
    private String eventType;
    private Integer policyId;
    private String actor;
    private LocalDateTime timestamp;
}
