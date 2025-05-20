package com.API.Documents_Management.Repositories;

import com.API.Documents_Management.Entities.Courriel;
import com.API.Documents_Management.Entities.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public interface FileRepo extends JpaRepository<File, Long> {

    Optional<File> findFileById(Long id);

    Optional<File> findByFileName(String fileName);

}
