package com.API.Documents_Management.Repositories;

import com.API.Documents_Management.Entities.Courriel;
import com.API.Documents_Management.Entities.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourrielRepo extends JpaRepository<Courriel, Long> {



    @Query("SELECT c FROM Courriel c LEFT JOIN FETCH c.courrielFiles WHERE c.courrielNumber = :number")
    Optional<Courriel> findByCourrielNumberWithFiles(@Param("number") String number);


    Boolean existsByCourrielNumber(String courrielNumber);

    // Find by existing file

    List<Courriel> findByCourrielFilesContaining(File file);

}
