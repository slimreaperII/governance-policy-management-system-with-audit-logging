package com.ms_service.governanceservice.outbox;

public enum SagaStatus {
    STARTED,
    FAILED,
    SUCCEEDED,
    PROCESSING,
}