package com.ms_service.auditservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false)
    private Integer policyId;
    @Column(nullable = false)
    private String actor;
    @Column(nullable = false)
    private LocalDateTime timeStamp;
}
