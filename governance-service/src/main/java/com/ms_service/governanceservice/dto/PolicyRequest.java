package com.ms_service.governanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {
    private String title;
    private String description;
    private String createdBy;
}
