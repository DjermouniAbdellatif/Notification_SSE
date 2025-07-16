package com.API.Documents_Management.Notification.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationMessage {
    private String email;
    private String divisionName;
    private String directionName;
    private String sousDirectionName;
    private String message;
    private String courrielNumber;
    private Set<String> filesNames;
    private String operation;
    private String time;

}