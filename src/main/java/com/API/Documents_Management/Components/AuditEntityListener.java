package com.API.Documents_Management.Components;

import com.API.Documents_Management.Entities.AuditableEntity;
import com.API.Documents_Management.Entities.Courriel;
import com.API.Documents_Management.Entities.File;
import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Services.AuditLogService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditEntityListener {

    private AuditLogService getAuditLogService() {
        return SpringContext.getBean(AuditLogService.class);
    }

    @PostPersist
    public void logPersist(Object entity) {

        if (entity instanceof Courriel) {
            handleAudit(entity, Operations.CREATE);
        } else if (entity instanceof File) {
            handleAudit(entity, Operations.UPLOAD_FILE);
        }
    }


    @PostUpdate
    public void logUpdate(Object entity) {
        handleAudit(entity, Operations.UPDATE);
    }

    @PostRemove
    public void logRemove(Object entity) {

        if (entity instanceof Courriel) {
            handleAudit(entity, Operations.DELETE);
        } else if (entity instanceof File) {
            handleAudit(entity, Operations.DELETE_FILE);
        }
    }

    private void handleAudit(Object entity, Operations operation) {
        if (!(entity instanceof AuditableEntity auditable)) {
            return;
        }

        String username = getCurrentUsername();
        String resource = entity.getClass().getSimpleName();
        String identifier = auditable.getAuditIdentifier();
        LocalDateTime now = LocalDateTime.now();

        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        String actionVerb = switch (operation) {
            case CREATE       -> "create";
            case UPDATE       -> "update";
            case DELETE       -> "delete";
            case UPLOAD_FILE  -> "upload";
            case DELETE_FILE  -> "delete";
        };

        String description = String.format(
                "User '%s' %s %s '%s' on %s at %s",
                username,
                actionVerb,
                resource,
                identifier,
                date,
                time
        );

        getAuditLogService().logAction(username, operation, resource,identifier, description);
    }




    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "SYSTEM";
    }
}
