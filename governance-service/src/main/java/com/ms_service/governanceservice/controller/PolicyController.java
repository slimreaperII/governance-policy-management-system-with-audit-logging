package com.ms_service.governanceservice.controller;

import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.service.PolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse createPolicy (@RequestBody PolicyRequest request){
        return policyService.createPolicy(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PolicyResponse> getAllPolicy (){
        return policyService.getAllPolicy();
    }
}
