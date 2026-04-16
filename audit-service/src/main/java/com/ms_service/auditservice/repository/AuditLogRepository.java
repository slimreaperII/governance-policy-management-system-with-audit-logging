package com.ms_service.auditservice.repository;

import com.ms_service.auditservice.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findAllByPolicyId (Integer id);
}
