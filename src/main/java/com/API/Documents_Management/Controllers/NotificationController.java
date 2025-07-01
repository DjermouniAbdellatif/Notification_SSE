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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationWebSocketService notificationService;
    private final AuthService authService;
    private final NotificationRepo notificationRepo;

    @GetMapping("/all")
    public ResponseEntity<List<NotificationEntity>> getAllNotifications() {
        AppUser currentUser = authService.getAuthenticatedUser();
        List<NotificationEntity> notifications = notificationService.getAllNotifications(currentUser);
        return ResponseEntity.ok(notifications);
    }

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

    @PutMapping("/mark-as-read/{id}")
    public ResponseEntity<List<NotificationEntity>> markAsReadAndGetUnread(@PathVariable Long id) {
        AppUser currentUser = authService.getAuthenticatedUser();

        // Récupérer la notification à marquer
        NotificationEntity notification = notificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Vérification facultative : appartient bien à la hiérarchie
        if (notification.getEmail().equalsIgnoreCase(currentUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // ne pas marquer sa propre notif
        }

        // Marquer comme lue
        notification.setRead(true);
        notificationRepo.save(notification);

        // Retourner la liste mise à jour
        List<NotificationEntity> unread = notificationService.getUnreadNotifications(currentUser);
        return ResponseEntity.ok(unread);
    }


}
