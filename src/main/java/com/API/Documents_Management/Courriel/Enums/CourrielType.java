package com.API.Documents_Management.Courriel.Enums;

public enum CourrielType {
    ARRIVER,
    DEPART;

    public static CourrielType fromStringSafe(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Le type de courriel ne peut pas être vide.");
        }

        try {
            return CourrielType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type de courriel invalide : " + value);
        }
    }
}
