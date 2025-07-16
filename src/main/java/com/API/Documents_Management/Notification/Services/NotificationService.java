package com.API.Documents_Management.Notification.Services;


import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionRepo;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Division.DivisionRepo;
import com.API.Documents_Management.Entities.AppUser;

import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Enums.RoleType;
import com.API.Documents_Management.Exceptions.UserNotFoundException;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Notification.Dto.NotificationDTO;
import com.API.Documents_Management.Notification.Entities.UserNotification;
import com.API.Documents_Management.Notification.Mapper.NotificationMapper;
import com.API.Documents_Management.Notification.Repositories.NotificationRepo;

import com.API.Documents_Management.SousDirection.SousDierctionRepo;
import com.API.Documents_Management.SousDirection.SousDirection;
import com.API.Documents_Management.Notification.Dto.NotificationMessage;
import com.API.Documents_Management.Notification.Entities.NotificationEntity;
import com.API.Documents_Management.Notification.Repositories.UserNotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final AppUserRepo userRepo;
    private final DirectionRepo directionRepo;
    private final SousDierctionRepo sousDierctionRepo;
    private final DivisionRepo divisionRepo;
    private final AppUserRepo appUserRepo;
    private final UserNotificationRepo userNotificationRepo;
    private final NotificationMapper notificationMapper;

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();


    public SseEmitter registerEmitter(String username) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(username.toLowerCase(), k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.get(username.toLowerCase()).remove(emitter));
        emitter.onTimeout(() -> emitters.get(username.toLowerCase()).remove(emitter));
        emitter.onError(e -> emitters.get(username.toLowerCase()).remove(emitter));

        return emitter;
    }

    // OK Tested
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
            List<AppUser> recipients = resolveRecipientsByHierarchy(sender);

            // Envoyer la notification par WebSocket à chaque destinataire
            recipients.forEach(user -> {
                List<SseEmitter> userEmitters = emitters.getOrDefault(user.getUsername().toLowerCase(), List.of());
                for (SseEmitter emitter : userEmitters) {
                    try {
                        emitter.send(SseEmitter.event().name("notification").data(notificationMsg));
                    } catch (Exception ex) {
                        emitters.get(user.getUsername().toLowerCase()).remove(emitter);
                    }
                }
            });
        });
    }



    // Ok Tested
    @Transactional
    public void saveNotification(NotificationMessage message) {
        Division division = (message.getDivisionName() == null ? null : divisionRepo.findByName(message.getDivisionName()).get());
        Direction direction = (message.getDirectionName() == null ? null : directionRepo.findByName(message.getDirectionName()).get());
        SousDirection sousDirection = (message.getSousDirectionName() == null ? null : sousDierctionRepo.findByName(message.getSousDirectionName()).get());

        // check if sender exist
        Optional<AppUser> senderOpt=appUserRepo.findAppUsersByUsernameWithRoles(message.getEmail());

        if(senderOpt.isPresent()) {
            AppUser sender = senderOpt.get();

            // get Notification recipients by sender's hierarchy
            List<AppUser> recipients = resolveRecipientsByHierarchy(sender);

            // Save Notification ->  notification table
            NotificationEntity notification = NotificationEntity.builder()
                    .email(message.getEmail())
                    .divisionId(division != null ? division.getId() : null)
                    .directionId(direction != null ? direction.getId() : null)
                    .sousDirectionId(sousDirection != null ? sousDirection.getId() : null)
                    .message(message.getMessage())
                    .courrielNumber(message.getCourrielNumber())
                    .filesNames(message.getFilesNames())
                    .operation(message.getOperation())
                    .time(message.getTime())
                    .build();
            notificationRepo.save(notification);

            // Save Notification + user  ->  UserNotification table
            recipients.forEach(recipient -> {
                UserNotification userNotification=UserNotification.builder()
                        .notification(notification)
                        .user(recipient)
                        .build();
                userNotificationRepo.save(userNotification);
            });

        }else{
            throw new UserNotFoundException("User not found with username: " + message.getEmail());
        }

    }

   public List<NotificationDTO> getAllNotifications() {

           List<NotificationEntity>notifications= notificationRepo.findAll();
           List<NotificationDTO> notificationDTOS= notifications.stream().map(notificationMapper::toDto).collect(Collectors.toList());

        return ((notifications.isEmpty()) ? new ArrayList<>() : notificationDTOS);

    }


    // OK Tested
    @Transactional
    public List<NotificationDTO> getNotificationsForUser(AppUser currentUser) {
        return userNotificationRepo.findByUser(currentUser)
                .stream()
                .map(UserNotification::getNotification)
                .map(notificationMapper::toDto)
                .toList();
    }

    // OK Tested
    @Transactional
    public List<NotificationDTO> readAllNotificationsByUser(AppUser currentUser) {

       List<UserNotification>userNotifications = userNotificationRepo.findByUser(currentUser);

       userNotificationRepo.deleteAll(userNotifications);

       return   userNotifications.stream()
               .map(UserNotification::getNotification)
               .map(notificationMapper::toDto)
               .collect(Collectors.toList());
    }

    // OK Tested
    @Transactional
    public NotificationDTO readNotificationById(Long id, AppUser currentUser) {
        Optional<UserNotification> userNotification = userNotificationRepo.findByNotificationIdAndUser(id,currentUser);

        if(userNotification.isPresent()) {
            NotificationEntity notificationEntity = userNotification.get().getNotification();
            userNotificationRepo.delete(userNotification.get());
            return notificationMapper.toDto(notificationEntity);
        }else{
            return null;
        }

        }




    // OK  Tested
    public List<AppUser> resolveRecipientsByHierarchy(AppUser sender) {
        List<AppUser> recipients = new ArrayList<>();
        UserHierarchyRole senderRole = determineHierarchyRole(sender);

        switch (senderRole) {
            case SIMPLE_USER -> {
                recipients.addAll(userRepo.findAllWithRolesByDirection(sender.getDirection()).stream()
                        .filter(u -> determineHierarchyRole(u) == UserHierarchyRole.DIRECTEUR)
                        .toList());
            }
            case DIRECTEUR -> {
                // Autres directeurs de la même direction
                recipients.addAll(userRepo.findAllWithRolesByDirection(sender.getDirection()).stream()
                        .filter(u -> determineHierarchyRole(u) == UserHierarchyRole.DIRECTEUR
                                && !u.getUsername().equalsIgnoreCase(sender.getUsername()))
                        .toList());

                // Chef de division
                recipients.addAll(userRepo.findAllWithRolesByDivision(sender.getDivision()).stream()
                        .filter(u -> determineHierarchyRole(u) == UserHierarchyRole.CHEF_DIVISION)
                        .toList());
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


    @Transactional
    public int deleteOrphanNotifications() {
        List<Long> orphanIds = notificationRepo.findOrphanNotificationIds();

        if (orphanIds.isEmpty()) {
            return 0;
        }

        // Supprimer  les entrées dans la table des filesNames
        notificationRepo.deleteFilesNamesByNotificationIds(orphanIds);

        //  supprimer les notifications orphelines elles-mêmes
        return notificationRepo.deleteNotificationsByIds(orphanIds);
    }


}
