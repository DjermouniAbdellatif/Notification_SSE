package com.API.Documents_Management.Components;

import com.API.Documents_Management.Services.CourrielService;
import com.API.Documents_Management.WebSocket.Services.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DatabaseCleanupScheduler {

    private final CourrielService courrielService;
    private final NotificationWebSocketService notificationService;

    // Methode will be executed every thursday at 12h00

    @Scheduled(cron = "0 0 12 1 * ?", zone = "Africa/Algiers")
    public void scheduledDatabaseCleanup() {
        System.out.println("[SCHEDULER] " + LocalDateTime.now() + " - Cleaning orphan files and courriels from database...");
        courrielService.cleanDatabaseFromMissingDiskData();

        int deletedCount = notificationService.deleteOrphanNotifications();
        System.out.println("Scheduler: " + deletedCount + " - Cleaning orphan  notifications...");


        System.out.println("[SCHEDULER] " + LocalDateTime.now() + " - Cleanup finished.");
    }
}
