package com.API.Documents_Management.Init;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionRepo;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Division.DivisionRepo;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Enums.HierarchyLevel;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.SousDirection.SousDierctionRepo;
import com.API.Documents_Management.SousDirection.SousDirection;
import com.API.Documents_Management.Utils.UserHierarchyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AddRunner {



    private final AppUserRepo appUserRepo;
    private final DivisionRepo divisionRepo;
    private final DirectionRepo directionRepo;
    private final SousDierctionRepo sousDirectionRepo;

    @Bean(name = "addDataRunner")
    CommandLineRunner commandLineRunner() {
      return args -> {


            Division division = divisionRepo.findById(1L).get();

          appUserRepo.findAllByDirection_DivisionWithDivisionLoaded(division).stream()
                  .forEach(u->System.out.println(u.getDivision().getName()));



       };
   }
}
