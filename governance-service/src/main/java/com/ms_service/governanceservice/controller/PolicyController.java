package com.ms_service.governanceservice.controller;

import com.ms_service.governanceservice.dto.PolicyEvent;
import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.policy.Status;
import com.ms_service.governanceservice.service.PolicyService;
import jakarta.validation.Valid;
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
    public PolicyResponse createPolicy (@RequestBody @Valid PolicyRequest request){
        return policyService.createPolicy(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PolicyResponse> getAllPolicy (){
        return policyService.getAllPolicy();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PolicyResponse getPolicy (@PathVariable Integer id) {
        return policyService.getPolicyByID(id);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<PolicyResponse> searchForPolicies (@RequestParam(required = false) String title, @RequestParam(required = false) String creator, @RequestParam(required = false) Status status) {
        return policyService.searchPolicies(title, creator, status);
    }

    @PostMapping("/{id}/submit")
    public PolicyResponse submitPolicy (@PathVariable Integer id) {
        return policyService.submitPolicy(id);
    }

    @DeleteMapping("/{id}")
    public String deletePolicy (@PathVariable Integer id) { return policyService.deletePolicy(id); }

    @PostMapping("/{id}/approve")
    public PolicyResponse approvePolicy (@PathVariable Integer id) {
        return policyService.approvePolicy(id);
    }

    @PostMapping("/{id}/reject")
    public PolicyResponse rejectPolicy (@PathVariable Integer id) {
        return policyService.rejectPolicy(id);
    }

    @GetMapping("/{id}/logs")
    public List<PolicyEvent> auditLogs (@PathVariable Integer id) {
        return policyService.getAllAuditLogsByPolicyId(id);
    }
}
