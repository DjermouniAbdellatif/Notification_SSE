package com.API.Documents_Management.Direction;

import com.API.Documents_Management.Division.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectionRepo extends JpaRepository<Direction,Long> {
    Optional<Direction> findByName(String name);

    List<Direction> findAllByDivision(Division division);

    @Query("SELECT d FROM Direction d JOIN FETCH d.division")
    List<Direction> findAllWithDivision();

    @Query("SELECT d FROM Direction d JOIN FETCH d.division WHERE d.division = :division")
    List<Direction> findByDivisionWithFetch(@Param("division") Division division);



    @Query("SELECT d FROM Direction d JOIN FETCH d.division WHERE d.id = :id")
    Optional<Direction> findByIdWithDivision(@Param("id") Long id);
}
