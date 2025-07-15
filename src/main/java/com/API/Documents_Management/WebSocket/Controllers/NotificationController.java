package com.API.Documents_Management.WebSocket.Controllers;

import com.API.Documents_Management.Dto.ApiResponse;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Services_Impl.CustomUserDetails;
import com.API.Documents_Management.Utils.UserUtil;
import com.API.Documents_Management.WebSocket.Dto.NotificationDTO;
import com.API.Documents_Management.WebSocket.Services.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationWebSocketService notificationService;


    /*
         Get Authenticated User Notifications
         ****** OK Tested ******
      */
    @GetMapping("/all")
        public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotificationsForUser() {

        List<NotificationDTO> data = notificationService.getAllNotifications();
        boolean success=(!data.isEmpty());
        String msg=(success?"Notifications Trouvées ":"Aucune notification Trouvée !");

        return ResponseEntity.ok(new ApiResponse<>(success,msg,data));

    }

    /*
        Get Unread User's Notifications
        ****** OK Tested ******
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsForUser(@AuthenticationPrincipal CustomUserDetails currentUserDetails) {
        AppUser currentUser = UserUtil.getAuthenticatedUser();

        var data = notificationService.getNotificationsForUser(currentUser);
        boolean success=(!data.isEmpty());
        String msg=(success?"Notifications Trouvées ":"Aucune notification Trouvée pour l'utilisateur :"+currentUser.getUsername());

        return ResponseEntity.ok(new ApiResponse<>(success,msg,data));

    }

    /*
       Read and delete User's Notifications by ID
       ****** OK Tested ******
    */
    @PutMapping("/read/{id}")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsReadById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails) {

            AppUser currentUser = UserUtil.getAuthenticatedUser();
            NotificationDTO data= notificationService.readNotificationById(id,currentUser);
            boolean success= data == null;
            String msg=(success?"Notification Lue avec Succès":"Aucune notification n'a été trouvée !");
            return ResponseEntity.ok(new ApiResponse<>(success,msg,data));
    }


    /*
       Read and delete All User's Notifications
       ****** OK Tested ******
    */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails currentUserDetails) {
        AppUser currentUser = UserUtil.getAuthenticatedUser();

        List<NotificationDTO>data= notificationService.readAllNotificationsByUser(currentUser);

        boolean success=(!data.isEmpty());
        String msg=(success?"Notifications Lues avec Succès":"Aucune notification trouvée pour l'utilisateur :"+currentUser.getUsername());

        return ResponseEntity.ok(new ApiResponse<>(success,msg,data));
    }


}
