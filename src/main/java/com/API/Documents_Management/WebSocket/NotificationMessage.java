package com.API.Documents_Management.WebSocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String resource;
    private String operation;
    private String time;

}