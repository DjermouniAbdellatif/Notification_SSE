package com.API.Documents_Management.WebSocket.Repositories;

import com.API.Documents_Management.WebSocket.Entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface NotificationRepo extends JpaRepository<NotificationEntity, Long> {

    // Récupérer les IDs de notifications sans correspondance dans UserNotification
    @Query(value = """
        SELECT n.id 
        FROM notifications n 
        WHERE NOT EXISTS (
            SELECT 1 FROM user_notification u WHERE u.notification_id = n.id
        )
        """, nativeQuery = true)
    List<Long> findOrphanNotificationIds();

    // Supprimer les entrées filesNames liées aux notifications données
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM notification_entity_files_names WHERE notification_entity_id IN :ids", nativeQuery = true)
    void deleteFilesNamesByNotificationIds(@Param("ids") List<Long> notificationIds);

    // Supprimer les notifications par Ids
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM notifications WHERE id IN :ids", nativeQuery = true)
    int deleteNotificationsByIds(@Param("ids") List<Long> notificationIds);
}