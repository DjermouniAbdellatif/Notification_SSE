package com.API.Documents_Management.Courriel.Repos;

import com.API.Documents_Management.Courriel.Entities.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepo extends JpaRepository<File, Long> {

    Optional<File> findFileById(Long id);

    Optional<File> findByFileName(String fileName);

}
