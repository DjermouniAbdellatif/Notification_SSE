package com.API.Documents_Management.Utils;

import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Entities.Courriel;
import com.API.Documents_Management.Enums.HierarchyLevel;

public class NotificationTargetChecker {

    public static boolean shouldNotify(AppUser user, Courriel courriel) {
        HierarchyLevel level = UserUtil.getUserHierarchy(user);

        return switch (level) {
            case CHEF_DIVISION ->
                    courriel.getFromDivision() != null && courriel.getFromDivision().equals(user.getDivision());

            case DIRECTEUR ->
                    (courriel.getFromDirection() != null && courriel.getFromDirection().equals(user.getDirection())) ||
                            (courriel.getFromSousDirection() != null && courriel.getFromSousDirection().getDirection().equals(user.getDirection()));

            case SOUS_DIRECTEUR ->
                    courriel.getFromSousDirection() != null && courriel.getFromSousDirection().equals(user.getSousDirection());

            default -> false;
        };
    }
}