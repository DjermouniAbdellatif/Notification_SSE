package com.API.Documents_Management.Notification.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String email;
    private String DivisionName;
    private String DirectionName;
    private String SousDirectionName;
    private String message;
    private String courrielNumber;
    private Set<String> filesNames;
    private String operation;

    private String time;
}