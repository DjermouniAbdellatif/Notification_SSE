package com.API.Documents_Management.Courriel.Repos;

import com.API.Documents_Management.Courriel.Entities.Courriel;
import com.API.Documents_Management.Courriel.Entities.File;
import com.API.Documents_Management.Courriel.Enums.CourrielType;
import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.SousDirection.SousDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourrielRepo extends JpaRepository<Courriel, Long>, JpaSpecificationExecutor<Courriel> {


    @Query("SELECT c FROM Courriel c LEFT JOIN FETCH c.courrielFiles WHERE c.courrielNumber = :number")
    Optional<Courriel> findByCourrielNumberWithFiles(@Param("number") String number);

    @Query("SELECT DISTINCT c FROM Courriel c LEFT JOIN FETCH c.courrielFiles")
    List<Courriel> findAllWithFiles();


    // Pour les courriels DEPART
    boolean existsByCourrielNumberAndCourrielTypeAndFromDivision_Id(
            String courrielNumber, CourrielType courrielType, Long divisionId);

    boolean existsByCourrielNumberAndCourrielTypeAndFromDirection_Id(
            String courrielNumber, CourrielType courrielType, Long directionId);

    boolean existsByCourrielNumberAndCourrielTypeAndFromSousDirection_Id(
            String courrielNumber, CourrielType courrielType, Long sousDirectionId);

    // Pour les courriels ARRIVER via les destinations
    @Query("SELECT COUNT(c) > 0 FROM Courriel c JOIN c.destinations d " +
            "WHERE c.courrielNumber = :courrielNumber " +
            "AND c.courrielType = :courrielType " +
            "AND d.toDivision.id = :divisionId")
    boolean existsByCourrielNumberAndCourrielTypeAndToDivision(
            @Param("courrielNumber") String courrielNumber,
            @Param("courrielType") CourrielType courrielType,
            @Param("divisionId") Long divisionId);

    @Query("SELECT COUNT(c) > 0 FROM Courriel c JOIN c.destinations d " +
            "WHERE c.courrielNumber = :courrielNumber " +
            "AND c.courrielType = :courrielType " +
            "AND d.toDirection.id = :directionId")
    boolean existsByCourrielNumberAndCourrielTypeAndToDirection(
            @Param("courrielNumber") String courrielNumber,
            @Param("courrielType") CourrielType courrielType,
            @Param("directionId") Long directionId);

    @Query("SELECT COUNT(c) > 0 FROM Courriel c JOIN c.destinations d " +
            "WHERE c.courrielNumber = :courrielNumber " +
            "AND c.courrielType = :courrielType " +
            "AND d.toSousDirection.id = :sousDirectionId")
    boolean existsByCourrielNumberAndCourrielTypeAndToSousDirection(
            @Param("courrielNumber") String courrielNumber,
            @Param("courrielType") CourrielType courrielType,
            @Param("sousDirectionId") Long sousDirectionId);
}
