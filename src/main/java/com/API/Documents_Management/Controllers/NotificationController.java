package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Repositories.NotificationRepo;
import com.API.Documents_Management.Services.AuthService;
import com.API.Documents_Management.Services_Impl.CustomUserDetails;
import com.API.Documents_Management.WebSocket.NotificationEntity;
import com.API.Documents_Management.WebSocket.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
     * ✅ Marquer toutes les notifications non lues comme lues
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<String> markAllAsRead(@AuthenticationPrincipal CustomUserDetails currentUserDetails) {
        var currentUser = currentUserDetails.getUser();
        notificationService.markAllUnreadNotificationsAsRead(currentUser);
        return ResponseEntity.ok("All unread notifications have been marked as read");
    }

    /**
     * ✅ Marquer une notification spécifique comme lue (par ID)
     */
    @PutMapping("/mark-read/{id}")
    public ResponseEntity<String> markAsReadById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails) {

        var currentUser = currentUserDetails.getUser();
        boolean success = notificationService.markNotificationAsReadById(id, currentUser);

        if (success) {
            return ResponseEntity.ok("Notification marked as read");
        } else {
            return ResponseEntity.badRequest().body("Notification not found or not accessible");
        }
    }
    }
