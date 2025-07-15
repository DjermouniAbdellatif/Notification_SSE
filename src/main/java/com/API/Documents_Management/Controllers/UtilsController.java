package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Services.CourrielService;
import com.API.Documents_Management.WebSocket.Services.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/db")
public class UtilsController {

    private final CourrielService courrielService;
    private final NotificationWebSocketService notificationService;

    @PutMapping("/cleanup")
    public void cleanUpDatabase(){

        System.out.println("[SCHEDULER] " + LocalDateTime.now() + " - Cleaning orphan files and courriels from database...");
        courrielService.cleanDatabaseFromMissingDiskData();

        int deletedCount = notificationService.deleteOrphanNotifications();
        System.out.println("Scheduler: " + deletedCount + " - Cleaning orphan  notifications...");


        System.out.println("[SCHEDULER] " + LocalDateTime.now() + " - Cleanup finished.");
    }
}
