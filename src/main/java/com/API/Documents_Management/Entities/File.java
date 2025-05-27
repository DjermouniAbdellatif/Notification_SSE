package com.API.Documents_Management.Entities;


import com.API.Documents_Management.Components.AuditEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditEntityListener.class)
public class File implements AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;


    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getAuditIdentifier() {
        return this.fileName;
    }
}