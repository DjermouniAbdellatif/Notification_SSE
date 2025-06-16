package com.API.Documents_Management.WebSocket;



import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Enums.RoleType;
import com.API.Documents_Management.Repositories.RoleRepo;
import com.API.Documents_Management.WebSocket.NotificationMessage;
import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Repositories.AppUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AppUserRepo userRepo;

    public void sendNotification(String title, String resource, Operations operation, String creator) {
        userRepo.findAppUserByUsername(creator).ifPresent(sender -> {
            String timestamp = LocalDateTime.now().toString();

            NotificationMessage message = NotificationMessage.builder()
                    .email(sender.getUsername())
                    .divisionName(sender.getDivision() != null ? sender.getDivision().getName() : null)
                    .directionName(sender.getDirection() != null ? sender.getDirection().getName() : null)
                    .sousDirectionName(sender.getSousDirection() != null ? sender.getSousDirection().getName() : null)
                    .message(title)
                    .resource(resource)
                    .operation(operation.name())
                    .time(timestamp)
                    .build();

            Set<String> recipients = resolveRecipientsByHierarchy(sender);
            recipients.forEach(username -> {
                System.out.println("📢 Notification envoyée à : " + username);
                messagingTemplate.convertAndSend("/topic/notifications/" + username.toLowerCase(), message);
            });
        });
    }


    private Set<String> resolveRecipientsByHierarchy(AppUser sender) {
        Set<String> recipients = new HashSet<>();

        boolean isAdmin = isAdmin(sender);
        boolean hasDivision = sender.getDivision() != null;
        boolean hasDirection = sender.getDirection() != null;
        boolean hasSousDirection = sender.getSousDirection() != null;

        // Cas 1 - If simple user de sous-direction
        if (hasDivision && hasDirection && hasSousDirection && !isAdmin) {
            // ➜ Notify directeur of direction
            userRepo.findAllByDirection(sender.getDirection()).stream()
                    .filter(user -> isAdmin(user) && user.getSousDirection() == null)
                    .map(AppUser::getUsername)
                    .forEach(recipients::add);
        }

        // Cas 2- if  Sender is un other user (ex : directeur)

        // Cas : Other User (not simple user) send notification (ex: directeur)
        else {
            // Get all directeurs of same direction (and  sous-directions)
            if (hasDivision && hasDirection) {
                recipients.addAll(userRepo.findAllByDirection(sender.getDirection()).stream()
                        .filter(user -> isAdmin(user) && user.getSousDirection() == null)
                        .map(AppUser::getUsername)
                        .collect(Collectors.toSet()));
            }

            // All  chefs  division of same division
            if (hasDivision) {
                recipients.addAll(userRepo.findAllByDivision(sender.getDivision()).stream()
                        .filter(user -> isAdmin(user)
                                && user.getDirection() == null
                                && user.getSousDirection() == null)
                        .map(AppUser::getUsername)
                        .collect(Collectors.toSet()));
            }
        }

        // Supprimer Sende from liste
        recipients.remove(sender.getUsername());

        return recipients;
    }


    private boolean isAdmin(AppUser user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleType.ADMIN));
    }
}
