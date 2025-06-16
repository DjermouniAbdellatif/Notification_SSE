package com.API.Documents_Management.Division;

import com.API.Documents_Management.Direction.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisionRepo extends JpaRepository<Division,Long> {
}
