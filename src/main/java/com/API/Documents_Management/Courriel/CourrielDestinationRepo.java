package com.API.Documents_Management.Courriel;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Entities.AlgerianMinistry;
import com.API.Documents_Management.SousDirection.SousDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourrielDestinationRepo extends JpaRepository<CourrielDestination, Long> {

    boolean existsByCourriel_CourrielNumberAndToDivision_IdAndToDirectionIsNullAndToSousDirectionIsNull(String courrielNumber, Long divisionId);

    boolean existsByCourriel_CourrielNumberAndToDirection_IdAndToSousDirectionIsNull(String courrielNumber, Long directionId);

    boolean existsByCourriel_CourrielNumberAndToSousDirection_Id(String courrielNumber, Long sousDirectionId);

    // Trouver toutes les destinations d'un courriel donné

    List<CourrielDestination> findByCourriel_CourrielNumber(String courrielNumber);

    // Trouver une destination d'un courriel pour une structure spécifique
    Optional<CourrielDestination> findByCourriel_CourrielNumberAndToDivisionAndToDirectionAndToSousDirectionAndToExternal(
            String courrielNumber,
            Division toDivision,
            Direction toDirection,
            SousDirection toSousDirection,
            AlgerianMinistry toExternal
    );

    // Vérifier s’il reste des destinations pour ce courriel
    boolean existsByCourriel(Courriel courriel);
}