package com.API.Documents_Management.SousDirection;

import com.API.Documents_Management.Direction.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SousDierctionRepo extends JpaRepository<SousDirection,Long> {
    Optional<SousDirection> findByName(String name);

    List<SousDirection> findAllByDirection(Direction direction);

    @Query("SELECT sd FROM SousDirection sd JOIN FETCH sd.direction d JOIN FETCH d.division")
    List<SousDirection> findAllWithDirectionAndDivision();



    @Query("SELECT sd FROM SousDirection sd JOIN FETCH sd.direction d JOIN FETCH d.division WHERE d = :direction")
    List<SousDirection> findByDirectionWithFetch(@Param("direction") Direction direction);

    @Query("SELECT sd FROM SousDirection sd JOIN FETCH sd.direction d JOIN FETCH d.division WHERE sd.id = :id")
    Optional<SousDirection> findByIdWithDirection(@Param("id") Long id);
}
