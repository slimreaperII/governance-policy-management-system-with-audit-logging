package com.ms_service.governanceservice.service;

import com.ms_service.governanceservice.dto.PolicyEvent;
import com.ms_service.governanceservice.dto.PolicyRequest;
import com.ms_service.governanceservice.dto.PolicyResponse;
import com.ms_service.governanceservice.kafka.KafkaProducer;
import com.ms_service.governanceservice.policy.Policy;
import com.ms_service.governanceservice.policy.Status;
import com.ms_service.governanceservice.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
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
    class searchPoliciesTest {
        private Policy policy1;
        private Policy policy2;
        private Policy policy3;
        private Policy policy4;

        @BeforeEach
        void setUp () {
            policy1 = new Policy(1, "title 1", "desc 1", Status.DRAFT, "admin 1", LocalDateTime.now());
            policy2 = new Policy(2, "title 2", "desc 2", Status.ACCEPTED, "admin 2", LocalDateTime.now());
            policy3 = new Policy(3, "title 1", "desc 3", Status.PENDING_APPROVAL, "admin 1", LocalDateTime.now());
            policy4 = new Policy(4, "title 4", "desc 4", Status.DRAFT, "admin 2", LocalDateTime.now());
        }

        @Test
        void shouldReturnPoliciesWhenStatusNotProvided () {
            //given
            String title = "title 1";
            String creator = "admin 1";
            when(policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title,creator)).thenReturn(List.of(policy1, policy3));

            //when
            List<PolicyResponse> result = policyService.searchPolicies(title, creator, null);

            //then
            assertNotNull(result);
            assertEquals(policy1.getPolicyId(), result.getFirst().getPolicyId());
            assertEquals(policy3.getStatus(), result.get(1).getStatus());
            verify(policyRepository, times(1)).findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title, creator);
        }

        @Test
        void shouldReturnPoliciesWhenStatusIsProvided () {
            //given
            String title = "title 4";
            String creator = "admin 2";
            Status status = Status.DRAFT;
            when(policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCaseAndStatus(title, creator, status)).thenReturn(List.of(policy4));

            //when
            List<PolicyResponse> result = policyService.searchPolicies(title, creator, status);

            //then
            assertNotNull(result);
            assertEquals(policy4.getPolicyId(), result.getFirst().getPolicyId());
            verify(policyRepository, times(1)).findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCaseAndStatus(title, creator, status);
        }

        @Test
        void shouldReturnAllWhenNothingIsProvided () {
            //given
            when(policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase("","")).thenReturn(List.of(policy1, policy2, policy3,policy4));

            //when
            List<PolicyResponse> result = policyService.searchPolicies(null, null, null);

            //then
            assertNotNull(result);
            assertEquals(policy1.getPolicyId(), result.getFirst().getPolicyId());
            verify(policyRepository, times(1)).findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase("", "");
        }

        @Test
        void shouldReturnPoliciesWhenOnlyTitleIsProvided () {
            //given
            String title = "title 1";
            when(policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title, "")).thenReturn(List.of(policy1, policy3));

            //when
            List<PolicyResponse> result = policyService.searchPolicies(title, null, null);

            //then
            assertNotNull(result);
            assertEquals(policy1.getTitle(), result.getFirst().getTitle());
            verify(policyRepository, times(1)).findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title, "");
        }

        @Test
        void shouldReturnPoliciesWhenOnlyCreatorIsProvided () {
            //given
            String creator = "admin 2";
            when(policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase("", creator)).thenReturn(List.of(policy2, policy4));

            //when
            List<PolicyResponse> result = policyService.searchPolicies(null, creator, null);

            //then
            assertNotNull(result);
            assertEquals(policy2.getCreatedBy(), result.getFirst().getCreatedBy());
            verify(policyRepository, times(1)).findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase("", creator);
        }

        @Test
        void shouldReturnPoliciesWhenOnlyStatusIsProvided (){
            //given
            Status status = Status.DRAFT;
            when(policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCaseAndStatus("", "", status)).thenReturn(List.of(policy1, policy4));

            //when
            List<PolicyResponse> result = policyService.searchPolicies(null, null, status);

            //then
            assertNotNull(result);
            assertEquals(policy1.getPolicyId(), result.getFirst().getPolicyId());
            assertEquals(policy1.getStatus(), result.getFirst().getStatus());
            verify(policyRepository, times(1)).findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCaseAndStatus("", "", status);
        }

        @Test
        void shouldReturnEmptyListWhenNoPoliciesFound() {
            //given
            String title = "title";
            when(policyRepository.findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title, "")).thenReturn(List.of());

            //when
            List<PolicyResponse> result = policyService.searchPolicies(title, "", null);

            //then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(policyRepository, times(1)).findByTitleContainingIgnoreCaseAndCreatedByContainingIgnoreCase(title, "");
        }
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
