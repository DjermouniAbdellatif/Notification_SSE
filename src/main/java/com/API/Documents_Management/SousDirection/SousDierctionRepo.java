package com.API.Documents_Management.SousDirection;

import com.API.Documents_Management.Direction.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SousDierctionRepo extends JpaRepository<SousDirection,Long> {
    Optional<SousDirection> findByName(String name);

}
