package com.API.Documents_Management.Division;

import com.API.Documents_Management.Direction.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DivisionRepo extends JpaRepository<Division,Long> {
    Optional<Division> findByName(String name);
}
