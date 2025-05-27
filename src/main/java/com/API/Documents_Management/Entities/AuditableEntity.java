package com.API.Documents_Management.Entities;

public interface AuditableEntity {

    Long getId();

    // Courriel number or filename
    String getAuditIdentifier();
}
