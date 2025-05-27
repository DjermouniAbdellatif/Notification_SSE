package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Dto.ApiResponse;
import com.API.Documents_Management.Dto.AuditLogResponse;
import com.API.Documents_Management.Services.AuditLogService;
import com.API.Documents_Management.Services.CourrielService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CourrielService courrielService;
    private final AuditLogService auditLogService;

    @DeleteMapping("/clean-db")
    public ResponseEntity<String> cleanDb() {
        courrielService.cleanDatabaseFromMissingDiskData();
        return ResponseEntity.ok("Database cleaned from orphaned files and courriels.");
    }


    @GetMapping("/audit")
    public ApiResponse<List<AuditLogResponse>> getAllAuditLogs() {

        List<AuditLogResponse> data=new ArrayList<>();
        boolean isSuccess=true;
        String message;
        HttpStatus status;
        try {
            data=auditLogService.getAllAuditLogs();
            message="All AuditLogs Successfully Founded ";
            status=HttpStatus.OK;

        }catch (Exception e) {
            isSuccess=false;
            message=e.getMessage();
            status=HttpStatus.CONFLICT;
        }

        return new  ApiResponse<List<AuditLogResponse>>(isSuccess,message,data);
    }

}