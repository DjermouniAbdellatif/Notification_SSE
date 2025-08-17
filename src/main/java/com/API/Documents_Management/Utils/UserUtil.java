package com.API.Documents_Management.Utils;


import com.API.Documents_Management.Courriel.Entities.Structure;
import com.API.Documents_Management.Courriel.Repos.CourrielRepo;
import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionRepo;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Entities.AppUser;

import com.API.Documents_Management.Enums.HierarchyLevel;
import com.API.Documents_Management.Exceptions.UserNotFoundException;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Services_Impl.CustomUserDetails;
import com.API.Documents_Management.SousDirection.SousDierctionRepo;
import com.API.Documents_Management.SousDirection.SousDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserUtil {

    private final CourrielRepo courrielRepo;
    private final DirectionRepo directionRepo;
    private final SousDierctionRepo sousDierctionRepo;
    private final AppUserRepo userRepo;


//    public  AppUser getAuthenticatedUser() {
//
//        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
//            return userDetails.getUser();
//        }
//        throw new RuntimeException("No authenticated user found");
//    }


    public AppUser getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsernameWithStructures(username) // OK si bien implémentée
                .orElseThrow(() -> new UserNotFoundException("Utilisateur avec username " + username + " non trouvé"));
    }

    //=============================== Hierarchy Utils =====================================

    //  GET user structure

    public   Structure getStructure(AppUser currentUser) {
        Structure structure = null;
        if(currentUser.getSousDirection() != null){
            structure = currentUser.getSousDirection();
        } else if (currentUser.getDirection() != null) {
            structure = currentUser.getDirection();
        } else if (currentUser.getDivision() != null) {
            structure = currentUser.getDivision();
        }
        return structure;
    }

    // Get user Structure name (exemple : Division DGB )
    public String resolveStructureString(AppUser user) {

        String name;
        String structure;

        if (user.getSousDirection() != null) {
            structure="Sous Direction";
            name=user.getSousDirection().getName();
        } else if (user.getDirection() != null) {
            structure="Direction";
            name=user.getDirection().getName();
        }else{
            structure="Division";
            name=user.getDivision().getName();
        }

        return structure+" "+name;
    }

    // Get user Hierarchy ( exemple : SUPER_ADMIN,CHEF_DIVISION .....)

    public HierarchyLevel getUserHierarchy(AppUser user) {
        boolean hasDivision = user.getDivision() != null;
        boolean hasDirection = user.getDirection() != null;
        boolean hasSousDirection = user.getSousDirection() != null;

        boolean isOnlyUser = user.getRoles().stream()
                .allMatch(role -> role.getName().toString().equalsIgnoreCase("USER"));

        boolean isSuperAdmin = user.getRoles().stream()
                .allMatch(role -> role.getName().toString().equalsIgnoreCase("SUPER_ADMIN"));

        if (!hasDivision && !hasDirection && !hasSousDirection && isSuperAdmin) return HierarchyLevel.SUPER_ADMIN;
        if (hasDivision && !hasDirection && !hasSousDirection) return HierarchyLevel.CHEF_DIVISION;
        if (hasDivision && hasDirection && !hasSousDirection) return HierarchyLevel.DIRECTEUR;
        if (hasDivision && hasDirection && hasSousDirection) return HierarchyLevel.SOUS_DIRECTEUR;
        if (isOnlyUser) return HierarchyLevel.SIMPLE_USER;

        throw new IllegalStateException("User with invalid  hierarchy");
    }


    // Get Les Structure sur les quelles le user est autorisée
    @Transactional(readOnly = true)
    public Set<Structure> getStructuresAutorisees(AppUser currentUser, HierarchyLevel userLevel) {
        Set<Structure> autorisees = new HashSet<>();

        switch (userLevel) {
            case SUPER_ADMIN -> {
                return Collections.emptySet();
            }

            case CHEF_DIVISION -> {
                Division division = currentUser.getDivision();
                autorisees.add(division);


                List<Direction> directions = directionRepo.findByDivisionWithFetch(division);
                autorisees.addAll(directions);

            }

            case DIRECTEUR -> {
                Direction direction = directionRepo.findByIdWithDivision(currentUser.getDirection().getId())
                        .orElseThrow(() -> new RuntimeException("Direction introuvable"));
                autorisees.add(direction);

                List<SousDirection> sousDirections = sousDierctionRepo.findByDirectionWithFetch(direction);
                autorisees.addAll(sousDirections);
            }

            case SOUS_DIRECTEUR -> {
                SousDirection sousDirection = sousDierctionRepo.findByIdWithDirection(currentUser.getSousDirection().getId())
                        .orElseThrow(() -> new RuntimeException("Sous-direction introuvable"));
                autorisees.add(sousDirection);
            }

            case SIMPLE_USER -> {
                return Collections.emptySet();
            }

            default -> throw new IllegalStateException("Niveau hiérarchique inconnu : " + userLevel);
        }

        return autorisees;
    }




    public boolean isSimpleUser(AppUser user) {
        return user.getRoles().contains("USER")
                && user.getSousDirection() != null
                && user.getDirection() != null
                && user.getDivision() != null;
    }


    public String getStructureName(Structure structure) {
        if (structure instanceof Division d) {
            return "Divison : "+d.getName();
        } else if (structure instanceof Direction d) {
            return "Direction : "+d.getName();
        } else if (structure instanceof SousDirection sd) {
            return "Sous Direction : "+sd.getName();
        } else {
            return "Structure inconnue";
        }
    }



}