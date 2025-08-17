package com.API.Documents_Management.Init;

import com.API.Documents_Management.Courriel.Entities.Structure;
import com.API.Documents_Management.Courriel.Enums.CourrielType;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Exceptions.AlreadyExistsException;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Notification.Services.NotificationService;
import com.API.Documents_Management.Courriel.Services.CourrielService;
import com.API.Documents_Management.Utils.StructurePrinterService;
import com.API.Documents_Management.Utils.UserUtil;
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
    private final UserUtil userUtil;
    private final StructurePrinterService printerService;

    @Bean(name = "addDataRunner")
    CommandLineRunner commandLineRunner() {
        return args -> {

            AppUser currentUser = userRepository.findByUsernameWithStructures("faycal@gmail.com").orElse(null);

            if (currentUser == null) {
                System.out.println("❌ User not found !");
            } else {

                System.out.println("\n======================== STRUCTURE ==================================");
                printerService.afficherStructures(currentUser);

                System.out.println("\n======================== EXISTENCE TEST =============================");


                Structure structure = userUtil.getStructure(currentUser);

                if(courrielService.existsInCurrentUserStructure("CF_1",CourrielType.ARRIVER,currentUser)) {
                    System.out.println("\n❌ Courriel 'CF_1' existe déjà dans la " + userUtil.getStructureName(structure));

                } else {

                    System.out.println("\n✅ Courriel 'CF_1' n'existe pas encore dans la " + userUtil.getStructureName(structure));

                }
            }
        };
    }

}
