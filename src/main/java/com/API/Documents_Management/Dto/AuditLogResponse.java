package com.API.Documents_Management.Dto;

import com.API.Documents_Management.Enums.Operations;

public record AuditLogResponse(Operations operation,
                               String resource,
                               String identifier,
                               String user,
                               String description,
                               String operationTime) {
}
