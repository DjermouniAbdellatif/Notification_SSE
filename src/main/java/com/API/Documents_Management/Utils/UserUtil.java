package com.API.Documents_Management.Utils;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Enums.HierarchyLevel;
import com.API.Documents_Management.Services_Impl.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {


    public static AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        throw new RuntimeException("No authenticated user found");
    }

    // Get user Hierarchy
    public static HierarchyLevel getUserHierarchy(AppUser user) {
        boolean hasDivision = user.getDivision() != null;
        boolean hasDirection = user.getDirection() != null;
        boolean hasSousDirection = user.getSousDirection() != null;

        boolean isOnlyUser = user.getRoles().stream()
                .allMatch(role -> role.getName().toString().equalsIgnoreCase("USER"));

        if (hasDivision && !hasDirection && !hasSousDirection) return HierarchyLevel.CHEF_DIVISION;
        if (hasDivision && hasDirection && !hasSousDirection) return HierarchyLevel.DIRECTEUR;
        if (hasDivision && hasDirection && hasSousDirection) return HierarchyLevel.SOUS_DIRECTEUR;
        if (isOnlyUser) return HierarchyLevel.SIMPLE_USER;

        throw new IllegalStateException("User with invalid  hierarchy");
    }
}