package com.API.Documents_Management.Init;

import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Notification.Services.NotificationService;
import com.API.Documents_Management.Courriel.Services.CourrielService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddRunner {

    private final AppUserRepo userRepository;
    private final CourrielService courrielService;
    private final NotificationService notificationService;
    private final AppUserRepo userRepo;

    @Bean(name = "addDataRunner")
    CommandLineRunner commandLineRunner() {

            return args -> {
                System.out.println("\nApplication started successfully...");

//                String courrielNumber = "CF_100";
//                String username = "asma@gmail.com";
//
//                AppUser currentUser = userRepository.findAppUserByUsername(username).orElse(null);
//
//                if (currentUser == null) {
//                    System.out.println("\n❌ User with username '" + username + "' not found !");
//                    return;
//                }
//
//                System.out.println("\n✅ User found : " + currentUser.getUsername());
//
//
//                System.out.println("\n🔎 User Hierarchy:");
//                System.out.println("Division = " + (currentUser.getDivision() != null ? currentUser.getDivision().getId() : "null"));
//                System.out.println("Direction = " + (currentUser.getDirection() != null ? currentUser.getDirection().getId() : "null"));
//                System.out.println("SousDirection = " + (currentUser.getSousDirection() != null ? currentUser.getSousDirection().getId() : "null"));
//
//                boolean exist = courrielService.isDuplicate(courrielNumber, currentUser);
//
//                if (exist) {
//                    System.out.println("\n✅ Courriel "+courrielNumber+" Already Exists!");
//                } else {
//                    System.out.println("\n❌ Courriel "+courrielNumber+" Not Exists .");
//                }
            };
        }
    }

