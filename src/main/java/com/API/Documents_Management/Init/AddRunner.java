package com.API.Documents_Management.Init;

import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Notification.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddRunner {

    private final NotificationService notificationService;
    private final AppUserRepo userRepo;

    @Bean(name = "addDataRunner")
    CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("\nApplication started successfully...");

        };
    }

}