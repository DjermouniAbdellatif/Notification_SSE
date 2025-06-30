package com.API.Documents_Management.SousDirection;

import com.API.Documents_Management.Direction.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SousDierctionRepo extends JpaRepository<SousDirection,Long> {
    SousDirection findByName(String name);

}
