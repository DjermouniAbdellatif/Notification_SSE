package com.API.Documents_Management.Courriel;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AlgerianMinistries {
    @Schema(description = "Ministère de la Défense Nationale")
    MINISTERE_DE_LA_DEFENSE_NATIONALE("Ministère de la Défense Nationale"),

    @Schema(description = "Ministère des Affaires Étrangères, de la Communauté Nationale à l’Étranger et des Affaires Africaines")
    MINISTERE_DES_AFFAIRES_ETRANGERES_ET_DE_LA_COMMUNAUTE_NATIONALE_A_LETRANGER_ET_DES_AFFAIRES_AFRICAINES("Ministère des Affaires Étrangères, de la Communauté Nationale à l’Étranger et des Affaires Africaines"),

    @Schema(description = "Ministère de l’Énergie, des Mines et des Énergies Renouvelables")
    MINISTERE_DE_L_ENERGIE_DES_MINES_ET_DES_ENERGIES_RENOUVELABLES("Ministère de l’Énergie, des Mines et des Énergies Renouvelables"),

    @Schema(description = "Ministère de l’Intérieur, des Collectivités Locales et de l’Aménagement du Territoire")
    MINISTERE_DE_L_INTERIEUR_DES_COLLECTIVITES_LOCALES_ET_DE_L_AMENAGEMENT_DU_TERRITOIRE("Ministère de l’Intérieur, des Collectivités Locales et de l’Aménagement du Territoire"),

    @Schema(description = "Ministère de la Justice")
    MINISTERE_DE_LA_JUSTICE("Ministère de la Justice"),

    @Schema(description = "Ministère des Moudjahidine et des Ayants Droit")
    MINISTERE_DES_MOUDJAHIDINE_ET_DES_AYANTS_DROIT("Ministère des Moudjahidine et des Ayants Droit"),

    @Schema(description = "Ministère des Affaires Religieuses et des Wakfs")
    MINISTERE_DES_AFFAIRES_RELIGIEUSES_ET_DES_WAKFS("Ministère des Affaires Religieuses et des Wakfs"),

    @Schema(description = "Ministère de l’Enseignement Supérieur et de la Recherche Scientifique")
    MINISTERE_DE_L_ENSEIGNEMENT_SUPERIEUR_ET_DE_LA_RECHERCHE_SCIENTIFIQUE("Ministère de l’Enseignement Supérieur et de la Recherche Scientifique"),

    @Schema(description = "Ministère de l’Éducation Nationale")
    MINISTERE_DE_L_EDUCATION_NATIONALE("Ministère de l’Éducation Nationale"),

    @Schema(description = "Ministère de la Formation et de l’Enseignement Professionnels")
    MINISTERE_DE_LA_FORMATION_DE_L_ENSEIGNEMENT_PROFESSIONNELS("Ministère de la Formation et de l’Enseignement Professionnels"),

    @Schema(description = "Ministère de la Culture et des Arts")
    MINISTERE_DE_LA_CULTURE_ET_DES_ARTS("Ministère de la Culture et des Arts"),

    @Schema(description = "Ministère de la Jeunesse")
    MINISTERE_DE_LA_JEUNESSE("Ministère de la Jeunesse"),

    @Schema(description = "Ministère des Sports")
    MINISTERE_DES_SPORTS("Ministère des Sports"),

    @Schema(description = "Ministère de la Poste et des Télécommunications")
    MINISTERE_DE_LA_POSTE_ET_DES_TELECOMMUNICATIONS("Ministère de la Poste et des Télécommunications"),

    @Schema(description = "Ministère de la Solidarité Nationale, de la Famille et de la Condition de la Femme")
    MINISTERE_DE_LA_SOLIDARITE_NATIONALE_DE_LA_FAMILLE_ET_DE_LA_CONDITION_DE_LA_FEMME("Ministère de la Solidarité Nationale, de la Famille et de la Condition de la Femme"),

    @Schema(description = "Ministère de l’Industrie")
    MINISTERE_DE_L_INDUSTRIE("Ministère de l’Industrie"),

    @Schema(description = "Ministère de la Production Pharmaceutique")
    MINISTERE_DE_LA_PRODUCTION_PHARMACEUTIQUE("Ministère de la Production Pharmaceutique"),

    @Schema(description = "Ministère de l’Agriculture, du Développement Rural et de la Pêche")
    MINISTERE_DE_L_AGRICULTURE_DU_DEVELOPPEMENT_RURAL_ET_DE_LA_PECHE("Ministère de l’Agriculture, du Développement Rural et de la Pêche"),

    @Schema(description = "Ministère de l’Habitat, de l’Urbanisme et de la Ville")
    MINISTERE_DE_L_HABITAT_DE_L_URBANISME_ET_DE_LA_VILLE("Ministère de l’Habitat, de l’Urbanisme et de la Ville"),

    @Schema(description = "Ministère du Commerce Intérieur et de la Régulation du Marché National")
    MINISTERE_DU_COMMERCE_INTERIEUR_ET_LA_REGULATION_DU_MARCHE_NATIONAL("Ministère du Commerce Intérieur et de la Régulation du Marché National"),

    @Schema(description = "Ministère du Commerce Extérieur et de la Promotion des Exportations")
    MINISTERE_DU_COMMERCE_EXTERIEUR_ET_DE_LA_PROMOTION_DES_EXPORTATIONS("Ministère du Commerce Extérieur et de la Promotion des Exportations"),

    @Schema(description = "Ministère de la Communication")
    MINISTERE_DE_LA_COMMUNICATION("Ministère de la Communication"),

    @Schema(description = "Ministère des Travaux Publics et des Infrastructures de Base")
    MINISTERE_DES_TRAVAUX_PUBLICS_ET_DES_INFRASTRUCTURES_DE_BASE("Ministère des Travaux Publics et des Infrastructures de Base"),

    @Schema(description = "Ministère de l’Hydraulique")
    MINISTERE_DE_L_HYDRAULIQUE("Ministère de l’Hydraulique"),

    @Schema(description = "Ministère des Transports")
    MINISTERE_DES_TRANSPORTS("Ministère des Transports"),

    @Schema(description = "Ministère du Tourisme et de l’Artisanat")
    MINISTERE_DU_TOURISME_ET_DE_L_ARTISANAT("Ministère du Tourisme et de l’Artisanat"),

    @Schema(description = "Ministère de la Santé")
    MINISTERE_DE_LA_SANTE("Ministère de la Santé"),

    @Schema(description = "Ministère du Travail, de l’Emploi et de la Sécurité Sociale")
    MINISTERE_DU_TRAVAIL_DE_L_EMPLOI_ET_DE_LA_SECURITE_SOCIALE("Ministère du Travail, de l’Emploi et de la Sécurité Sociale"),

    @Schema(description = "Ministère des Relations avec le Parlement")
    MINISTERE_DES_RELATIONS_AVEC_LE_PARLEMENT("Ministère des Relations avec le Parlement"),

    @Schema(description = "Ministère de l’Environnement et de la Qualité de la Vie")
    MINISTERE_DE_L_ENVIRONNEMENT_ET_DE_LA_QUALITE_DE_LA_VIE("Ministère de l’Environnement et de la Qualité de la Vie"),

    @Schema(description = "Ministère de l’Économie de la Connaissance, des Start-up et des Microentreprises")
    MINISTERE_DE_L_ECONOMIE_DE_LA_CONNAISSANCE_DES_START_UP_ET_DES_MICRO_ENTREPRISES("Ministère de l’Économie de la Connaissance, des Start-up et des Microentreprises"),

    @Schema(description = "Ministère de la Numérisation et des Statistiques")
    MINISTERE_DE_NUMERISATION_ET_DES_STATISTIQUES("Ministère de la Numérisation et des Statistiques"),

    @Schema(description = "Service du Premier Ministre")
    SERVICE_DU_PREMIER_MINISTRE("Service du Premier Ministre"),

    @Schema(description = "Assemblée Populaire Nationale")
    ASSEMBLE_POPULAIRE_NATIONALE("Assemblée Populaire Nationale"),

    @Schema(description = "Conseil de la Nation")
    CONSEIL_DE_LA_NATION("Conseil de la Nation"),

    @Schema(description = "Cour Suprême")
    COUR_SUPREME("Cour Suprême"),

    @Schema(description = "Conseil d’État")
    CONSEIL_D_ETAT("Conseil d’État"),

    @Schema(description = "Cour Constitutionnelle")
    COUR_CONSTITUTIONNELLE("Cour Constitutionnelle"),

    @Schema(description = "Cour des Comptes")
    COUR_DES_COMPTES("Cour des Comptes"),

    @Schema(description = "Autorité Nationale Indépendante des Élections")
    AUTORITE_NATIONALE_INDEPENDANTE_DES_ELECTIONS("Autorité Nationale Indépendante des Élections"),

    @Schema(description = "Conseil National Économique, Social et Environnemental")
    CONSEIL_NATIONAL_ECONOMIQUE_SOCIAL_ET_ENVIRONNEMENTAL("Conseil National Économique, Social et Environnemental"),

    @Schema(description = "Conseil National des Droits de l’Homme")
    CONSEIL_NATIONAL_DES_DROITS_DE_L_HOMME("Conseil National des Droits de l’Homme"),

    @Schema(description = "Haut Conseil Islamique")
    HAUT_CONSEIL_ISLAMIQUE("Haut Conseil Islamique"),

    @Schema(description = "Haut Conseil de la Langue Arabe")
    HAUT_CONSEIL_DE_LA_LANGUE_ARABE("Haut Conseil de la Langue Arabe"),

    @Schema(description = "Observatoire National de la Société Civile")
    OBSERVATOIRE_NATIONAL_DE_LA_SOCIETE_CIVILE("Observatoire National de la Société Civile"),

    @Schema(description = "Académie Algérienne des Sciences et des Technologies")
    ACADEMIE_ALGERIENNE_DES_SCIENCES_ET_DES_TECHNOLOGIES("Académie Algérienne des Sciences et des Technologies"),

    @Schema(description = "Commission d’Organisation et de Surveillance des Opérations de Bourse")
    COMMISSION_D_ORGANISATION_ET_DE_SURVEILLANCE_DES_OPERATIONS_DE_BOURSE("Commission d’Organisation et de Surveillance des Opérations de Bourse"),

    @Schema(description = "Office Central de Répression de la Corruption")
    OFFICE_CENTRAL_DE_REPRESSION_DE_LA_CORRUPTION("Office Central de Répression de la Corruption"),

    @Schema(description = "Direction Générale de la Fonction Publique et de la Réforme Administrative")
    DIRECTION_GENERALE_DE_LA_FONCTION_PUBLIQUE_ET_DE_LA_REFORME_ADMINISTRATIVE("Direction Générale de la Fonction Publique et de la Réforme Administrative"),

    @Schema(description = "Conseil Supérieur de la Magistrature")
    CONSEIL_SUPERIEUR_DE_LA_MAGISTRATURE("Conseil Supérieur de la Magistrature"),

    @Schema(description = "Haut Commissariat à l’Amazighité")
    HAUT_COMMISSARIAT_A_L_AMAZIGHITE("Haut Commissariat à l’Amazighité");

    private final String displayName;

    AlgerianMinistries(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonValue
    public String toJson() {
        return displayName;
    }

    @JsonCreator
    public static AlgerianMinistries fromJson(String displayName) {
        return Arrays.stream(AlgerianMinistries.values())
                .filter(m -> m.displayName.equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ministry: " + displayName));
    }

}