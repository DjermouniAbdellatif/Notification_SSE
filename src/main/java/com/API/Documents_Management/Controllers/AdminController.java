package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Services.CourrielService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CourrielService courrielService;

    @DeleteMapping("/clean-db")
    public ResponseEntity<String> cleanDb() {
        courrielService.cleanDatabaseFromMissingDiskData();
        return ResponseEntity.ok("Database cleaned from orphaned files and courriels.");
    }
}