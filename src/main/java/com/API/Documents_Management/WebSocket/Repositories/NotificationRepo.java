package com.API.Documents_Management.WebSocket.Repositories;

import com.API.Documents_Management.WebSocket.Entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity, Long> {

}