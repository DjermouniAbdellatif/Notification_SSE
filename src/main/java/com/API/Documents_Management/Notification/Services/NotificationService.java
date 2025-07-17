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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
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



        emitter.onCompletion(() -> {
            emitters.get(username.toLowerCase()).remove(emitter);
            log.info("✅ Emitter terminé pour {}", username);
        });

        emitter.onTimeout(() -> {
            emitters.get(username.toLowerCase()).remove(emitter);
            log.warn("⏰ Timeout de l'emitter pour {}", username);
        });

        emitter.onError(e -> {
            emitters.get(username.toLowerCase()).remove(emitter);
            log.error("❌ Erreur sur l'emitter pour {}: {}", username, e.getMessage());
        });

        return emitter;
    }

    // OK TESTED
    @Transactional
    public void sendNotification(String message, String courrielNumber, Set<String> filesNames, Operations operation, String creator) {
        log.info("📨 Envoi de notification lancé par {}", creator);

        userRepo.findAppUserByUsername(creator).ifPresentOrElse(sender -> {
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

            log.info("🗂 Notification construite : {}", notificationMsg);

            // Sauvegarde
            saveNotification(notificationMsg);
            log.info("💾 Notification enregistrée en base de données pour {}", sender.getUsername());

            // Résolution des destinataires
            List<AppUser> recipients = resolveRecipientsByHierarchy(sender);
            log.info("👥 {} destinataire(s) identifié(s) pour la notification", recipients.size());

            recipients.forEach(user -> {
                String username = user.getUsername().toLowerCase();
                List<SseEmitter> userEmitters = emitters.getOrDefault(username, List.of());

                log.info("📡 Envoi vers {} ({} emitter(s))", username, userEmitters.size());

                userEmitters.forEach(emitter -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .id(UUID.randomUUID().toString())
                                        .name("notification")
                                        .data(notificationMsg)
                                        .reconnectTime(5000)
                        );
                        log.info("✅ Notification envoyée à {}", username);
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                        emitters.get(username).remove(emitter);
                        log.error("⚠️ Échec de l'envoi à {}: {}", username, ex.getMessage());
                    }
                });
            });

        }, () -> log.warn("⚠️ Utilisateur inconnu: {}", creator));
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
