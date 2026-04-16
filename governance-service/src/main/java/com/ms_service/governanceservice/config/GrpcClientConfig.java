package com.ms_service.governanceservice.config;

import com.ms_service.governanceservice.grpc.AuditLogServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Bean
    public AuditLogServiceGrpc.AuditLogServiceBlockingStub auditLogServiceBlockingStub (
            GrpcChannelFactory channelFactory
    ) {
        var channel = channelFactory.createChannel("localhost:9090");
        return AuditLogServiceGrpc.newBlockingStub(channel);
    }
}