package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Services_Impl.CustomUserDetails;
import com.API.Documents_Management.WebSocket.NotificationEntity;
import com.API.Documents_Management.WebSocket.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationWebSocketService notificationService;

    /**
     * 🔔 Récupérer toutes les notifications
     */
    @GetMapping("/all")
    public ResponseEntity<List<NotificationEntity>> getAllNotificationsForUser() {

        var notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    /**
     * 🔔 Récupérer toutes les notifications pour User
     */
    @GetMapping("/mine")
    public ResponseEntity<List<NotificationEntity>> getNotificationsForUser(@AuthenticationPrincipal CustomUserDetails currentUserDetails) {
        var currentUser = currentUserDetails.getUser();
        var notifications = notificationService.getNotificationsForUser(currentUser);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 🔔 Récupérer uniquement les notifications non lues
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications(@AuthenticationPrincipal CustomUserDetails currentUserDetails) {
        var currentUser = currentUserDetails.getUser();
        var unreadNotifications = notificationService.getUnreadNotifications(currentUser);
        return ResponseEntity.ok(unreadNotifications);
    }

    /**
     * ✅ Marquer toutes les notifications non lues comme lues et les supprimer
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<String> markAllAsRead(@AuthenticationPrincipal CustomUserDetails currentUserDetails) {
        var currentUser = currentUserDetails.getUser();
        notificationService.markAllUnreadNotificationsAsRead(currentUser);
        return ResponseEntity.ok("All read notifications have been deleted");
    }

    /**
     * ✅ Marquer une notification spécifique comme lue (par ID) et la supprimer
     */
    @PutMapping("/mark-read/{id}")
    public ResponseEntity<String> markAsReadById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails) {

        var currentUser = currentUserDetails.getUser();
        boolean success = notificationService.readNotificationById(id, currentUser);

        if (success) {
            return ResponseEntity.ok("Notification marked as read");
        } else {
            return ResponseEntity.badRequest().body("Notification not found or not accessible");
        }
    }
    }
