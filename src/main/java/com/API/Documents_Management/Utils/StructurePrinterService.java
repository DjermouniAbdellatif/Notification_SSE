package com.API.Documents_Management.Utils;

import com.API.Documents_Management.Courriel.Entities.Structure;
import com.API.Documents_Management.Entities.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class StructurePrinterService {

    private final UserUtil userUtil;

    @Transactional
    public void afficherStructures(AppUser currentUser) {

        Set<Structure> structures = userUtil.getStructuresAutorisees(currentUser, userUtil.getUserHierarchy(currentUser));

        System.out.println("\nUser " + currentUser.getUsername() + " with poste :" + userUtil.getUserHierarchy(currentUser));

        for (Structure structure : structures) {
            System.out.println("Structure : " + structure.getTypeStructure() + " " + userUtil.getStructureName(structure));
        }
    }
}
