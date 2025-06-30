package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Dto.*;
import com.API.Documents_Management.Entities.Courriel;
import com.API.Documents_Management.Services.CourrielService;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/courriels")
@RequiredArgsConstructor
public class CourrielController {

    private final CourrielService courrielService;

    //===================== Filter ===============================================

    @PostMapping("/filter")
    public ResponseEntity<Page<Courriel>> filterCourriels(
            @RequestBody CourrielFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Courriel> result = courrielService.filterCourriels(filterRequest, page, size);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateCourrielResponse>> createCourriel(
            @RequestParam("courrielNumber") String courrielNumber,
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        CreateCourrielRequest request = new CreateCourrielRequest(courrielNumber, files);
        ApiResponse<CreateCourrielResponse> response = courrielService.createCourriel(request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping
    public ResponseEntity<ApiResponse<DeleteCourrielResponse>> deleteCourriel(@RequestParam("courrielNumber") String courrielNumber) throws IOException {
        ApiResponse<DeleteCourrielResponse> response = courrielService.deleteCourrielByNumber(courrielNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String courrielNumber,
            @RequestParam String fileName) {
        return courrielService.downloadFile(courrielNumber, fileName);
    }

    // Update Courriel ( add or remove file )
    @PostMapping("/add-files")
    public ResponseEntity<ApiResponse<CreateCourrielResponse>>  addFiles(
            @RequestParam String courrielNumber,
            @RequestParam("files") List<MultipartFile> files
    )throws IOException {
        ApiResponse<CreateCourrielResponse> response = courrielService.addFilesToCourriel(courrielNumber, files);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove-file")
    public ResponseEntity<ApiResponse<DeleteFileResponse>> removeFile(
            @RequestParam String courrielNumber,
            @RequestParam String fileName
    ) {
        System.out.println("courrielNumber = " + courrielNumber);
        System.out.println("fileName = " + fileName);

        ApiResponse<DeleteFileResponse> response = courrielService.removeFileFromCourriel(courrielNumber, fileName );
        return ResponseEntity.ok(response);
    }

}