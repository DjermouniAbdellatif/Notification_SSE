package com.API.Documents_Management.Direction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectionRepo extends JpaRepository<Direction,Long> {
    Direction findByName(String name);
}
