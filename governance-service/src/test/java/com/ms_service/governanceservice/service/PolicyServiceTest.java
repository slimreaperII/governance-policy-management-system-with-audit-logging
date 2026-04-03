package com.ms_service.governanceservice.service;

import com.ms_service.governanceservice.dto.PolicyEvent;
import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.kafka.KafkaProducer;
import com.ms_service.governanceservice.policy.Policy;
import com.ms_service.governanceservice.policy.Status;
import com.ms_service.governanceservice.repository.PolicyRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private PolicyService policyService;

    @Nested
    class CreatePolicyTests {
        @Test
        void ShouldCreatePolicySuccessfully () {
            //Given
            PolicyRequest request = new PolicyRequest();
            request.setTitle("Test title");
            request.setDescription("Test Description");
            request.setCreatedBy("Test admin");

            when(policyRepository.existsByTitleAndCreatedByAndDescription(anyString(), anyString(), anyString())).thenReturn(false);

            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle(request.getTitle());
            policy.setDescription(request.getDescription());
            policy.setCreatedBy(request.getCreatedBy());
            policy.setStatus(Status.DRAFT);

            when(policyRepository.save(any(Policy.class))).thenReturn(policy);

            //When
            PolicyResponse testResponse = policyService.createPolicy(request);

            //Then
            assertNotNull(testResponse);
            assertEquals("Test title", testResponse.getTitle());
            verify(policyRepository, times(1)).existsByTitleAndCreatedByAndDescription(anyString(), anyString(), anyString());
            verify(policyRepository, times(1)).save(any(Policy.class));
            verify(kafkaProducer, times(1)).sendPolicyEvent(any(PolicyEvent.class));
        }

        @Test
        void ShouldThrowExceptionWhenDuplicatePolicyFound () {
            //Given
            PolicyRequest request = new PolicyRequest();
            request.setTitle("Test title");
            request.setDescription("Test Description");
            request.setCreatedBy("Test admin");

            when(policyRepository.existsByTitleAndCreatedByAndDescription(anyString(), anyString(), anyString())).thenReturn(true);

            //when
            //then
            assertThrows(ResponseStatusException.class, () ->
                    policyService.createPolicy(request));
        }
    }

    @Test
    void getAllPolicy() {
    }

    @Test
    void getPolicyByID() {
    }

    @Test
    void submitPolicy() {
    }

    @Test
    void approvePolicy() {
    }

    @Test
    void rejectPolicy() {
    }
}