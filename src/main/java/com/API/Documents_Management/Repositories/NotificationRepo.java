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

    List<NotificationEntity> findBySousDirectionId(Long sousDirectionId);

    List<NotificationEntity> findByDirectionId(Long directionId);

    List<NotificationEntity> findByDivisionIdAndSousDirectionIdIsNull(Long divisionId);

    List<NotificationEntity> findByReadFalseAndSousDirectionId(Long sousDirectionId);

    List<NotificationEntity> findByReadFalseAndDirectionId(Long directionId);

    List<NotificationEntity> findByReadFalseAndDivisionIdAndSousDirectionIdIsNull(Long divisionId);
}
