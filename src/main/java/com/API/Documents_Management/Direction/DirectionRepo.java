package com.API.Documents_Management.Direction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DirectionRepo extends JpaRepository<Direction,Long> {
    Optional<Direction> findByName(String name);
}
