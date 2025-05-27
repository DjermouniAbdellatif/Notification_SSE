package com.API.Documents_Management.Entities;

import com.API.Documents_Management.Components.AuditEntityListener;
import com.API.Documents_Management.Enums.Operations;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
@EntityListeners(AuditEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Operations operation;

    private String resource; //"Courriel" or "File"

    private String identifier;

    private String description;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime operationTime;

}