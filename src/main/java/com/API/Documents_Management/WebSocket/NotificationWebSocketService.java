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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepo notificationRepo;
    private final AppUserRepo userRepo;
    private final DirectionRepo directionRepo;
    private final SousDierctionRepo sousDierctionRepo;
    private final DivisionRepo divisionRepo;



    public List<NotificationEntity> getAllNotifications() {

       List<NotificationEntity>notifications= notificationRepo.findAll();

       return ((notifications.isEmpty()) ? new ArrayList<>() : notifications);

    }


    public List<NotificationEntity> getNotificationsForUser(AppUser currentUser) {

        List<NotificationEntity>notifications=new ArrayList<>();

        if (currentUser.getSousDirection() != null) {
            notifications= notificationRepo.findBySousDirectionId(currentUser.getSousDirection().getId());
        } else if (currentUser.getDirection() != null) {
            notifications= notificationRepo.findByDirectionId(currentUser.getDirection().getId());
        } else if (currentUser.getDivision() != null) {
            notifications= notificationRepo.findByDivisionIdAndSousDirectionIdIsNull(currentUser.getDivision().getId());
        }

        // Exclure les notifications créées par le user lui-même

        return notifications.stream()
                .filter(notification -> !notification.getEmail().equalsIgnoreCase(currentUser.getUsername()))
                .collect(Collectors.toList());
    }

    public List<NotificationEntity> getUnreadNotifications(AppUser currentUser) {
        return getNotificationsForUser(currentUser).stream()
                .filter(notification -> !notification.getEmail().equalsIgnoreCase(currentUser.getUsername()))
                .filter(notification -> !notification.isRead())
                .collect(Collectors.toList());
    }



    public void markAllUnreadNotificationsAsRead(AppUser currentUser) {
        // Récupérer toutes les notifications visibles pour le user
        List<NotificationEntity> visibleNotifications = getNotificationsForUser(currentUser);

        // Filtrer celles qui ne sont PAS encore lues
        List<NotificationEntity> unreadNotifications = visibleNotifications.stream()
                .filter(notification -> !notification.isRead())
                .collect(Collectors.toList());

        // Les marquer comme lues
        unreadNotifications.forEach(notification -> notification.setRead(true));

        // Puis supprimer directement
        notificationRepo.deleteAll(unreadNotifications);
    }

    public boolean readNotificationById(Long id, AppUser currentUser) {
        Optional<NotificationEntity> optionalNotification = notificationRepo.findById(id);

        if (optionalNotification.isPresent()) {
            NotificationEntity notification = optionalNotification.get();

            boolean belongsToUser = (
                    (currentUser.getSousDirection() != null && notification.getSousDirectionId() != null
                            && currentUser.getSousDirection().getId().equals(notification.getSousDirectionId()))
                            ||
                            (currentUser.getDirection() != null && notification.getDirectionId() != null
                                    && currentUser.getDirection().getId().equals(notification.getDirectionId()))
                            ||
                            (currentUser.getDivision() != null && notification.getDivisionId() != null
                                    && notification.getSousDirectionId() == null
                                    && currentUser.getDivision().getId().equals(notification.getDivisionId()))
            );

            if (belongsToUser) {
                notificationRepo.delete(notification);
                return true;
            }
        }

        return false;
    }

    public void sendNotification(String message, String courrielNumber, Set<String> filesNames, Operations operation, String creator) {
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

            // Sauvegarde la notification une seule fois dans la BDD
            saveNotification(notificationMsg);

            // Déterminer les destinataires en fonction de la hiérarchie
            Set<String> recipients = resolveRecipientsByHierarchy(sender);

            // Envoyer la notification par WebSocket à chaque destinataire
            recipients.forEach(username -> {
                messagingTemplate.convertAndSend("/topic/notifications/" + username.toLowerCase(), notificationMsg);
            });
        });
    }

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
                // Ne notifie personne
            }
        }

        return recipients;
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

    private boolean isAdmin(AppUser user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleType.ADMIN));
    }

}
