package com.API.Documents_Management.Init;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.WebSocket.Dto.NotificationDTO;
import com.API.Documents_Management.WebSocket.Entities.NotificationEntity;
import com.API.Documents_Management.WebSocket.Entities.UserNotification;
import com.API.Documents_Management.WebSocket.Mapper.NotificationMapper;
import com.API.Documents_Management.WebSocket.Services.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
@Component
@RequiredArgsConstructor
public class AddRunner {

    private final NotificationWebSocketService notificationService;
    private final AppUserRepo userRepo;

    @Bean(name = "addDataRunner")
    CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("\nApplication started successfully...");

        };
    }

}