package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Services.AuthService;
import com.API.Documents_Management.Services_Impl.CustomUserDetails;
import com.API.Documents_Management.WebSocket.NotificationEntity;
import com.API.Documents_Management.WebSocket.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationWebSocketService notificationService;
    private final AuthService authService;

    @GetMapping("/mine")
    public ResponseEntity<List<NotificationEntity>> getMyNotifications() {
        AppUser currentUser = authService.getAuthenticatedUser();
        List<NotificationEntity> notifications = notificationService.getNotificationsForUser(currentUser);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications() {
        AppUser currentUser = authService.getAuthenticatedUser();
        List<NotificationEntity> unread = notificationService.getUnreadNotifications(currentUser);
        return ResponseEntity.ok(unread);
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationEntity>> getAllNotifications() {

        List<NotificationEntity> allNotifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(allNotifications);
    }
}
