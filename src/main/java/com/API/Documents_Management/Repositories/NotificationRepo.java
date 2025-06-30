package com.API.Documents_Management.Repositories;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.WebSocket.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity, Long> {


    List<NotificationEntity> findByDirectionId(Long directionId);

    List<NotificationEntity> findByDivisionIdAndSousDirectionIdIsNull(Long divisionId);

    // Méthodes pour non lues

    @Query("""
    SELECT n FROM NotificationEntity n
    WHERE n.read = false
    AND (
        (:sousDirectionId IS NOT NULL AND n.sousDirectionId = :sousDirectionId)
        OR (:sousDirectionId IS NULL AND :directionId IS NOT NULL AND n.directionId = :directionId AND n.sousDirectionId IS NULL)
        OR (:sousDirectionId IS NULL AND :directionId IS NULL AND :divisionId IS NOT NULL AND n.divisionId = :divisionId AND n.directionId IS NULL AND n.sousDirectionId IS NULL)
    )
""")
    List<NotificationEntity> findUnreadNotificationsForUser(
            @Param("divisionId") Long divisionId,
            @Param("directionId") Long directionId,
            @Param("sousDirectionId") Long sousDirectionId
    );



    List<NotificationEntity> findByDirectionIdAndReadFalse(Long directionId);

    List<NotificationEntity> findBySousDirectionIdAndReadFalse(Long sousDirectionId);


    List<NotificationEntity> findByDivisionIdAndDirectionIdIsNullAndSousDirectionIdIsNullAndReadFalse(Long divisionId);

}