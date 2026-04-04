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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            policy.setCreatedAt(LocalDateTime.now());

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
            assertThrows(ResponseStatusException.class, () -> policyService.createPolicy(request));
            verify(policyRepository, times(1)).existsByTitleAndCreatedByAndDescription(anyString(), anyString(), anyString());
        }
    }

    @Test
    void getAllPolicy() {
        //given
        Policy policy1 = new Policy(1, "title 1", "desc 1", Status.DRAFT, "admin 1", LocalDateTime.now());
        Policy policy2 = new Policy(2, "title 2", "desc 2", Status.ACCEPTED, "admin 1", LocalDateTime.now());
        Policy policy3 = new Policy(3, "title 3", "desc 3", Status.PENDING_APPROVAL, "admin 2", LocalDateTime.now());

        when(policyRepository.findAll()).thenReturn(List.of(policy1, policy2, policy3));

        //when
        List<PolicyResponse> result = policyService.getAllPolicy();

        //then
        assertNotNull(result);
        assertEquals("title 1", result.getFirst().getTitle());
        verify(policyRepository, times(1)).findAll();
    }

    @Test
    void getPolicyByID() {
        //given
        Policy policy1 = new Policy(1, "title 1", "desc 1", Status.DRAFT, "admin 1", LocalDateTime.now());
        Policy policy2 = new Policy(2, "title 2", "desc 2", Status.ACCEPTED, "admin 1", LocalDateTime.now());
        Integer id = 2;

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy2));

        //when
        PolicyResponse response = policyService.getPolicyByID(id);

        //
        assertNotNull(response);
        assertEquals("title 2", response.getTitle());
        verify(policyRepository, times(1)).findById(anyInt());
    }


    @Nested
    class submitPolicyTest {
        @Test
        void shouldSubmitPolicySuccessfully () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.DRAFT);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
            when(policyRepository.save(any(Policy.class))).thenReturn(policy);

            //when
            PolicyResponse response = policyService.submitPolicy(1);

            //then
            assertNotNull(response);
            assertEquals("title 1", response.getTitle());
            assertEquals(Status.PENDING_APPROVAL, response.getStatus());
            verify(policyRepository, times(1)).save(any(Policy.class));
            verify(policyRepository, times(1)).findById(anyInt());
            verify(kafkaProducer, times(1)).sendPolicyEvent(any(PolicyEvent.class));
        }

        @Test
        void shouldThrowExceptionWhenStatusDontMatch () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.PENDING_APPROVAL);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

            //when//then
            assertThrows(ResponseStatusException.class, () -> policyService.submitPolicy(1));
            verify(policyRepository, times(1)).findById(anyInt());
        }
    }

    @Nested
    class deletePolicyTest {
        @Test
        void shouldDeletePolicySuccessfully () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.DRAFT);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

            //when
            String response = policyService.deletePolicy(1);

            //then
            assertNotNull(response);
            assertEquals("Policy deleted successfully", response);
            verify(policyRepository, times(1)).delete(any(Policy.class));
            verify(policyRepository, times(1)).findById(anyInt());
            verify(kafkaProducer, times(1)).sendPolicyEvent(any(PolicyEvent.class));
        }

        @Test
        void shouldThrowExceptionWhenStatusDontMatch () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.PENDING_APPROVAL);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

            //when//then
            assertThrows(ResponseStatusException.class, () -> policyService.deletePolicy(1));
            verify(policyRepository, times(1)).findById(anyInt());
        }
    }

    @Nested
    class approvePolicyTest {
        @Test
        void shouldApprovePolicySuccessfully () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.PENDING_APPROVAL);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
            when(policyRepository.save(any(Policy.class))).thenReturn(policy);

            //when
            PolicyResponse response = policyService.approvePolicy(1);

            //then
            assertNotNull(response);
            assertEquals("title 1", response.getTitle());
            assertEquals(Status.ACCEPTED, response.getStatus());
            verify(policyRepository, times(1)).save(any(Policy.class));
            verify(policyRepository, times(1)).findById(anyInt());
            verify(kafkaProducer, times(1)).sendPolicyEvent(any(PolicyEvent.class));
        }

        @Test
        void shouldThrowExceptionWhenStatusDontMatch () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.DRAFT);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

            //when//then
            assertThrows(ResponseStatusException.class, () -> policyService.approvePolicy(1));
            verify(policyRepository, times(1)).findById(anyInt());
        }
    }

    @Nested
    class rejectPolicyTest {
        @Test
        void shouldRejectPolicySuccessfully () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.PENDING_APPROVAL);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
            when(policyRepository.save(any(Policy.class))).thenReturn(policy);

            //when
            PolicyResponse response = policyService.rejectPolicy(1);

            //then
            assertNotNull(response);
            assertEquals("title 1", response.getTitle());
            assertEquals(Status.REJECTED, response.getStatus());
            verify(policyRepository, times(1)).save(any(Policy.class));
            verify(policyRepository, times(1)).findById(anyInt());
            verify(kafkaProducer, times(1)).sendPolicyEvent(any(PolicyEvent.class));
        }

        @Test
        void shouldThrowExceptionWhenStatusDontMatch () {
            //given
            Policy policy = new Policy();
            policy.setPolicyId(1);
            policy.setTitle("title 1");
            policy.setDescription("desc 1");
            policy.setCreatedBy("admin 1");
            policy.setStatus(Status.DRAFT);
            policy.setCreatedAt(LocalDateTime.now());

            when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

            //when//then
            assertThrows(ResponseStatusException.class, () -> policyService.rejectPolicy(1));
            verify(policyRepository, times(1)).findById(anyInt());
        }
    }
}
