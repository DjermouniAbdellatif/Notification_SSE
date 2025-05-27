package com.API.Documents_Management.Services;

import com.API.Documents_Management.Dto.AuditLogResponse;
import com.API.Documents_Management.Entities.AuditLog;
import com.API.Documents_Management.Entities.AuditableEntity;
import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Repositories.AuditLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String username, Operations operation, String resource,String identifier, String description) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setOperation(operation);
        log.setResource(resource);
        log.setIdentifier(resource + ": " + identifier);
        log.setDescription(description);
        log.setOperationTime(LocalDateTime.now());;

        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getAllAuditLogs() {

        List<AuditLog> logs = auditLogRepository.findAll();

        if (logs.isEmpty()) {
            throw new EntityNotFoundException("No Audit logs Found !");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return logs.stream()
                .map(log -> new AuditLogResponse(
                        log.getOperation(),
                        log.getResource(),
                        log.getIdentifier(),
                        log.getUsername(),
                        log.getDescription(),
                        log.getOperationTime().format(formatter)
                ))
                .toList();
    }
}