package com.API.Documents_Management.Components;

import com.API.Documents_Management.Services.CourrielService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DatabaseCleanupScheduler {

    private final CourrielService courrielService;

    // Methode will be executed every thursday at 15h30

    @Scheduled(cron = "0 20 15 ? * MON", zone = "Africa/Algiers")
    public void scheduledDatabaseCleanup() {
        System.out.println("[SCHEDULER] " + LocalDateTime.now() + " - Cleaning orphan files and courriels from database...");
        courrielService.cleanDatabaseFromMissingDiskData();
        System.out.println("[SCHEDULER] " + LocalDateTime.now() + " - Cleanup finished.");
    }
}
