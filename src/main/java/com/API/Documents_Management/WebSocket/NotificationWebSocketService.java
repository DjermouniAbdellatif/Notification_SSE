package com.API.Documents_Management.WebSocket;

import com.API.Documents_Management.Enums.Operations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(String msg,String resource, Operations operation, String creatorEmail) {
        NotificationMessage notif = NotificationMessage.builder()
                .message(msg)
                .resource(resource)
                .operation(operation.name())
                .creator(creatorEmail)
                .time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

        messagingTemplate.convertAndSend("/topic/notifications", notif);
    }
}
