package com.ms_service.auditservice.service;

import com.ms_service.auditservice.grpc.AuditLogRequest;
import com.ms_service.auditservice.grpc.AuditLogResponse;
import com.ms_service.auditservice.grpc.AuditLogResponseList;
import com.ms_service.auditservice.grpc.AuditLogServiceGrpc;
import com.ms_service.auditservice.model.AuditLog;
import com.ms_service.auditservice.repository.AuditLogRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
public class AuditLogServiceImpl extends AuditLogServiceGrpc.AuditLogServiceImplBase  {

    private final AuditLogRepository repository;

    public AuditLogServiceImpl(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void getAllPolicyLog(AuditLogRequest request, StreamObserver<AuditLogResponseList> responseObserver) {
        Integer reqPolicyId = request.getPolicyId();
        List<AuditLog> auditLogs = repository.findAllByPolicyId(reqPolicyId);

        List<AuditLogResponse> auditLogResponse = auditLogs.stream().map(auditLog -> AuditLogResponse.newBuilder()
                .setEventType(auditLog.getEventType())
                .setPolicyId(auditLog.getPolicyId())
                .setActor(auditLog.getActor())
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(auditLog.getTimeStamp()
                                .atZone(java.time.ZoneId.systemDefault()).toEpochSecond())
                        .setNanos(auditLog.getTimeStamp().getNano())
                        .build())
                .build()).toList();

        AuditLogResponseList auditLogResponseList = AuditLogResponseList.newBuilder().addAllAuditLogResponse(auditLogResponse).build();

        responseObserver.onNext(auditLogResponseList);
        responseObserver.onCompleted();
    }
}
