package com.API.Documents_Management.WebSocket;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "notifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private Long divisionId;
    private Long directionId;
    private Long sousDirectionId;
    private String message;
    private String courrielNumber;

    @ElementCollection(fetch = FetchType.LAZY)
    private Set<String> filesNames;

    private String operation;
    private String time;
    private boolean read;
}