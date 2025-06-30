package com.API.Documents_Management.WebSocket;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionRepo;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Division.DivisionRepo;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Enums.RoleType;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Repositories.NotificationRepo;
import com.API.Documents_Management.SousDirection.SousDierctionRepo;
import com.API.Documents_Management.SousDirection.SousDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AppUserRepo userRepo;
    private final NotificationRepo notificationRepo;
    private final DivisionRepo divisionRepo;
    private final DirectionRepo directionRepo;
    private final SousDierctionRepo sousDierctionRepo;


    // Methode to save notification
    public void saveNotification(NotificationMessage message) {

        Division division = (message.getDivisionName() == null ? null : divisionRepo.findByName(message.getDivisionName()));
        Direction direction = (message.getDirectionName() == null ? null : directionRepo.findByName(message.getDirectionName()));
        SousDirection sousDirection = (message.getSousDirectionName() == null ? null : sousDierctionRepo.findByName(message.getSousDirectionName()));

        NotificationEntity entity = NotificationEntity.builder()
                .email(message.getEmail())
                .divisionId(division != null ? division.getId() : null)
                .directionId(direction != null ? direction.getId() : null)
                .sousDirectionId(sousDirection != null ? sousDirection.getId() : null)
                .message(message.getMessage())
                .courrielNumber(message.getCourrielNumber())
                .filesNames(message.getFilesNames())
                .operation(message.getOperation())
                .time(message.getTime())
                .read(false)
                .build();

        notificationRepo.save(entity);
    }



    // Methode to get All Notifications
    public List<NotificationEntity> getAllNotifications() {

        return notificationRepo.findAll().stream().toList();
    }

    // Methode to get All Notifications By user
    public List<NotificationEntity> getNotificationsForUser(AppUser currentUser) {

        List<NotificationEntity> notifications=new ArrayList<>();
        // Cas 1 : Simple user => aucune notification

        if (currentUser.getSousDirection() != null) {
            return List.of(); // Simple user ne reçoit rien
        }
        // Cas 2 : Directeur => direction + sous-directions
        else if (currentUser.getDirection() != null) {
            notifications= notificationRepo.findByDirectionId(currentUser.getDirection().getId());
        }
        // Cas 3 : Chef de division => division + directions attachées (sans sous-directions)
        else if (currentUser.getDivision() != null) {
            notifications= notificationRepo.findByDivisionIdAndSousDirectionIdIsNull(currentUser.getDivision().getId());
        }

        // Filtrer pour enlever celles créées par soi-même
        return notifications.stream()
                .filter(n -> !n.getEmail().equalsIgnoreCase(currentUser.getUsername()))
                .collect(Collectors.toList());
    }

    // Méthode pour récupérer les notifications non lues d'un utilisateur donné
    @Transactional
    public List<NotificationEntity> getUnreadNotifications(AppUser user) {
        Long divisionId = user.getDivision() != null ? user.getDivision().getId() : null;
        Long directionId = user.getDirection() != null ? user.getDirection().getId() : null;
        Long sousDirectionId = user.getSousDirection() != null ? user.getSousDirection().getId() : null;

        List<NotificationEntity> unreadNotifications = notificationRepo.findUnreadNotificationsForUser(
                divisionId,
                directionId,
                sousDirectionId
        );

        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(unreadNotifications);

        return unreadNotifications;
    }




    // Methode to Send notification by hierarchy
    public void sendNotification(String message, String courrielNumber,Set<String>filesNames, Operations operation, String creator) {
        userRepo.findAppUserByUsername(creator).ifPresent(sender -> {
            String timestamp = LocalDateTime.now().toString();

            NotificationMessage notificationMsg = NotificationMessage.builder()
                    .email(sender.getUsername())
                    .divisionName(sender.getDivision() != null ? sender.getDivision().getName() : null)
                    .directionName(sender.getDirection() != null ? sender.getDirection().getName() : null)
                    .sousDirectionName(sender.getSousDirection() != null ? sender.getSousDirection().getName() : null)
                    .message(message)
                    .courrielNumber(courrielNumber)
                    .filesNames(filesNames)
                    .operation(operation.name())
                    .time(timestamp)
                    .build();

            // save notification
            saveNotification(notificationMsg);

            Set<String> recipients = resolveRecipientsByHierarchy(sender);
            recipients.forEach(username -> {
                messagingTemplate.convertAndSend("/topic/notifications/" + username.toLowerCase(), notificationMsg);
            });
        });
    }

    private enum UserHierarchyRole {
        CHEF_DIVISION,
        DIRECTEUR,
        SIMPLE_USER
    }

    private UserHierarchyRole determineHierarchyRole(AppUser user) {
        boolean hasDivision = user.getDivision() != null;
        boolean hasDirection = user.getDirection() != null;
        boolean hasSousDirection = user.getSousDirection() != null;


        if (hasDivision && !hasDirection && !hasSousDirection) {
            return UserHierarchyRole.CHEF_DIVISION;
        } else if (isAdmin(user) && hasDivision && hasDirection && !hasSousDirection) {
            return UserHierarchyRole.DIRECTEUR;
        } else if (hasDivision && hasDirection && hasSousDirection) {
            return UserHierarchyRole.SIMPLE_USER;
        }
        return UserHierarchyRole.SIMPLE_USER;
    }

    private Set<String> resolveRecipientsByHierarchy(AppUser sender) {
        Set<String> recipients = new HashSet<>();
        UserHierarchyRole senderRole = determineHierarchyRole(sender);

        switch (senderRole) {
            case SIMPLE_USER -> {

                recipients.addAll(userRepo.findAllWithRolesByDirection(sender.getDirection()).stream()
                        .filter(u -> determineHierarchyRole(u) == UserHierarchyRole.DIRECTEUR)
                        .map(AppUser::getUsername)
                        .collect(Collectors.toSet()));
            }

            case DIRECTEUR -> {


                // Autres directeurs de la même direction
                recipients.addAll(userRepo.findAllWithRolesByDirection(sender.getDirection()).stream()
                        .filter(u -> determineHierarchyRole(u) == UserHierarchyRole.DIRECTEUR
                                && !u.getUsername().equalsIgnoreCase(sender.getUsername()))
                        .map(AppUser::getUsername)
                        .collect(Collectors.toSet()));

                // Chef de division
                recipients.addAll(userRepo.findAllWithRolesByDivision(sender.getDivision()).stream()
                        .filter(u -> determineHierarchyRole(u) == UserHierarchyRole.CHEF_DIVISION)
                        .map(AppUser::getUsername)
                        .collect(Collectors.toSet()));
            }

            case CHEF_DIVISION -> {
            }
        }

        return recipients;
    }

    private boolean isAdmin(AppUser user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleType.ADMIN));
    }
}
