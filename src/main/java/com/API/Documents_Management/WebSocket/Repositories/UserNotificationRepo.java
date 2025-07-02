package com.API.Documents_Management.WebSocket.Repositories;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.WebSocket.Entities.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserNotificationRepo extends JpaRepository<UserNotification, Long> {

    @Query("SELECT un FROM UserNotification un LEFT JOIN FETCH un.notification WHERE un.user = :user")
    List<UserNotification> findByUser(AppUser user);

    @Query("SELECT un FROM UserNotification un LEFT JOIN FETCH un.notification ")
    List<UserNotification> findAll();

    @Query("SELECT un FROM UserNotification un LEFT JOIN FETCH un.notification WHERE un.notification.id=:notificationId and un.user = :user ")
    Optional<UserNotification> findByNotificationIdAndUser(Long notificationId, AppUser user);
}