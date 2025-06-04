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
    private String message;
    private String resource;
    private String operation;
    private String creator;
    private String time;
}