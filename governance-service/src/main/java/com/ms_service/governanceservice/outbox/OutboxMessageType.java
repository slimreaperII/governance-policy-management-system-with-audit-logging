package com.ms_service.governanceservice.outbox;

public enum OutboxMessageType {
    POLICY_CREATED,
    POLICY_SUBMITTED,
    POLICY_DELETED,
    POLICY_APPROVED,
    POLICY_REJECTED
}
