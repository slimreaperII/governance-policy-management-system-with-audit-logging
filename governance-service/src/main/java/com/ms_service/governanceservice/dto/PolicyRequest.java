package com.ms_service.governanceservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String createdBy;
}
