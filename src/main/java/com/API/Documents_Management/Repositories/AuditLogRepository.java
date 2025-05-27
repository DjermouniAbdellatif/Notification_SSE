package com.API.Documents_Management.Repositories;

import com.API.Documents_Management.Entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAll();
}
