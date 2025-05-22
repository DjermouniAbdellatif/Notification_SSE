package com.API.Documents_Management.Services;


import com.API.Documents_Management.Dto.*;
import com.API.Documents_Management.Entities.File;
import com.API.Documents_Management.Exceptions.*;
import com.API.Documents_Management.Exceptions.FileNotFoundException;
import com.API.Documents_Management.Repositories.*;
import com.API.Documents_Management.Entities.*;

import com.API.Documents_Management.Utils.FormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;


@Service
public class CourrielService {

    private final CourrielRepo courrielRepository;
    private final FileRepo fileRepository;
    private final String basePath;
    private final long maxFileSize;
    private final long totalMaxSize;
    private final CourrielRepo courrielRepo;

    public CourrielService(
            CourrielRepo courrielRepository,
            FileRepo fileRepository,
            @Value("${file.storagePath}") String basePath,
            @Value("${file.maxSize}") String maxFileSize,
            @Value("${file.totalMaxSize}") String totalMaxSize,
            CourrielRepo courrielRepo) {
        this.courrielRepository = courrielRepository;
        this.fileRepository = fileRepository;
        this.basePath = basePath;
        this.maxFileSize =convertSizeToBytes(maxFileSize);
        this.totalMaxSize = convertSizeToBytes(totalMaxSize);
        this.courrielRepo = courrielRepo;
    }

    public ApiResponse<CreateCourrielResponse> createCourriel(CreateCourrielRequest request) throws IOException {

        // Check if a courriel with the same number already exists

        if (courrielRepository.findByCourrielNumberWithFiles(request.courrielNumber()).isPresent()) {
            throw new AlreadyExistsException("Courriel with this number already exists!");
        }

        // Check if files list is null or empty
        List<MultipartFile> uploadedFiles = request.files();

        // Filter out empty file inputs (no filename or size == 0)
        List<MultipartFile> nonEmptyFiles = uploadedFiles.stream()
                .filter(f -> f != null && !f.getOriginalFilename().isBlank() && f.getSize() > 0)
                .toList();

        if (nonEmptyFiles.isEmpty()) {
            throw new IllegalArgumentException("No valid files uploaded. All files were empty or missing.");
        }

        // Check total size of all files

        long totalSize = request.files().stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > totalMaxSize) {
            throw new IllegalArgumentException("Total size of uploaded files exceeds the allowed limit of "
                    + totalMaxSize/(1024*1024)  + " MB.");
        }

        //  Create a directory to store files
        String sanitizedNumber = sanitize(request.courrielNumber());
        Path folderPath = Paths.get(basePath, sanitizedNumber);
        Files.createDirectories(folderPath);

        //  track valid and skipped files
        Set<File> courrielFiles = new HashSet<>();
        List<UploadFileResponse> validFiles = new ArrayList<>();
        List<SkippedFileError> skippedFiles = new ArrayList<>();

        //  Process each uploaded file
        for (MultipartFile multipartFile : nonEmptyFiles) {
            String originalName = Paths.get(multipartFile.getOriginalFilename()).getFileName().toString();

            // Validate file type

            if (!"application/pdf".equals(multipartFile.getContentType())) {
                skippedFiles.add(new SkippedFileError(originalName, "Only PDF files are allowed"));
                continue;
            }

            // Validate each file size
            if (multipartFile.getSize() > maxFileSize) {
                skippedFiles.add(new SkippedFileError(originalName, "File exceeds individual size limit"));
                continue;
            }


            String compressedFileName = request.courrielNumber() + "_" + originalName + ".gz";
            Path compressedFilePath = folderPath.resolve(compressedFileName);

            // Compress and write the file
            try (
                    InputStream input = new BufferedInputStream(multipartFile.getInputStream());
                    OutputStream output = new GZIPOutputStream(Files.newOutputStream(compressedFilePath))
            ) {
                input.transferTo(output);
            } catch (IOException e) {
                skippedFiles.add(new SkippedFileError(originalName, "Error occurred during compression"));
                continue;
            }

            // Build the file entity and response
            File fileEntity = File.builder()
                    .fileName(compressedFileName)
                    .filePath(compressedFilePath.toString())
                    .fileType("application/pdf")
                    .fileSize(Files.size(compressedFilePath))
                    .build();

            courrielFiles.add(fileEntity);

            UploadFileResponse response = UploadFileResponse.builder()
                    .fileName(compressedFileName)
                    .filePath(compressedFilePath.toString())
                    .fileSize(FormatUtils.formatFileSize(Files.size(compressedFilePath)))
                    .build();

            validFiles.add(response);
        }

        // If no valid files, delete the folder
        if (!courrielFiles.isEmpty()) {
            Courriel courriel = Courriel.builder()
                    .courrielNumber(request.courrielNumber())
                    .courrielType("PDF")
                    .courrielPath(folderPath.toString())
                    .courrielFiles(courrielFiles)
                    .build();
            courrielRepository.save(courriel);
        }else {
            Files.deleteIfExists(folderPath);
        }

        // return the  response
        CreateCourrielResponse response = CreateCourrielResponse.builder()
                .courrielNumber(request.courrielNumber())
                .uploadedFiles(validFiles)
                .skippedFiles(skippedFiles)
                .build();

        return new ApiResponse<>(
                true,"Courriel created successfully", response);
    }



    public ResponseEntity<Resource> downloadFile(String courrielNumber, String fileName) {

        // Get courriel data from DB
        Courriel courriel = courrielRepository.findByCourrielNumberWithFiles(courrielNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Courriel not found: " + courrielNumber));

        // Check if file exist
        File file = courriel.getCourrielFiles().stream()
                .filter(f -> f.getFileName().equalsIgnoreCase(fileName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileName));

        // Check if file exist physically
        Path filePath = Paths.get(file.getFilePath());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("File not found on disk: " + filePath);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath); // Can return null
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + file.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error while reading file: " + fileName, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to determine file type: " + fileName, e);
        }
    }




    public ApiResponse<DeleteCourrielResponse> deleteCourrielByNumber(String courrielNumber) {

        // Check if courriel exist
        Courriel courriel = courrielRepository
                .findByCourrielNumberWithFiles(courrielNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Courriel not found with number " + courrielNumber));

        // Get courriel path
        Path folderPath = Paths.get(courriel.getCourrielPath());


        try {
            if (Files.exists(folderPath)) {
                Files.walk(folderPath)
                        .sorted(Comparator.reverseOrder()) // Delete files first
                        .map(Path::toFile)
                        .forEach(f -> {
                            if (!f.delete()) {
                                System.err.println("Failed to delete: " + f.getAbsolutePath());
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete files for Courriel " + courrielNumber, e);
        }

        // Delete courriel from DB
        courrielRepository.delete(courriel);


        DeleteCourrielResponse uploadedFiles = DeleteCourrielResponse.builder()
                .courrielNumber(courriel.getCourrielNumber())
                .courrielPath(courriel.getCourrielPath())
                .build();

        return ApiResponse.<DeleteCourrielResponse>builder()
                .message("Courriel deleted successfully.")
                .data(uploadedFiles)
                .build();
    }


    public ApiResponse<CreateCourrielResponse> addFilesToCourriel(
            String courrielNumber,
            List<MultipartFile> filesToSave
    ) throws IOException {

        if (filesToSave == null || filesToSave.isEmpty()) {
            throw new EmptyFileException("No files provided.");
        }

        Courriel courriel = courrielRepository
                .findByCourrielNumberWithFiles(courrielNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Courriel not found: " + courrielNumber));

        Path folderPath = Paths.get(basePath, sanitize(courrielNumber));
        Files.createDirectories(folderPath);

        List<UploadFileResponse> uploadedFiles = new ArrayList<>();
        List<SkippedFileError> skippedFiles = new ArrayList<>();

        for (MultipartFile file : filesToSave) {
            if (file.isEmpty()) {
                skippedFiles.add(SkippedFileError.builder()
                        .fileName("unknown")
                        .reason("Empty file")
                        .build());
                continue;
            }

            String originalFileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String compressedFileName = courrielNumber + "_" + originalFileName + ".gz";
            Path targetPath = folderPath.resolve(compressedFileName);

            boolean existsOnDisk = Files.exists(targetPath);
            boolean existsInDb = courriel.getCourrielFiles().stream()
                    .anyMatch(f -> f.getFileName().equalsIgnoreCase(compressedFileName));

            if (existsOnDisk || existsInDb) {
                String reason;
                if (existsOnDisk) {
                    reason = "File exists on directory (" + folderPath.toString() + ")";
                } else {
                    reason = "File already exists in database.";
                }
                skippedFiles.add(SkippedFileError.builder()
                        .fileName(originalFileName)
                        .reason(reason)
                        .build());
                continue;
            }

            try (InputStream in = file.getInputStream();
                 OutputStream out = new GZIPOutputStream(Files.newOutputStream(targetPath))) {
                in.transferTo(out);
            }

            long compressedSize = Files.size(targetPath) ;

            File fileEntity = File.builder()
                    .fileName(compressedFileName)
                    .fileType("application/pdf")
                    .filePath(targetPath.toString())
                    .fileSize(compressedSize)
                    .build();

            courriel.getCourrielFiles().add(fileEntity);

            uploadedFiles.add(UploadFileResponse.builder()
                    .fileName(compressedFileName)
                    .filePath(targetPath.toString())
                    .fileSize(FormatUtils.formatFileSize(compressedSize))
                    .build());
        }

        courrielRepository.save(courriel);

        CreateCourrielResponse response = CreateCourrielResponse.builder()
                .courrielNumber(courrielNumber)
                .uploadedFiles(uploadedFiles)
                .skippedFiles(skippedFiles)
                .build();

        String message="Courriel : "+courrielNumber+" Updated.";

        if(uploadedFiles.isEmpty()) {
            message="Courriel Not Updated !";
        }

        return ApiResponse.<CreateCourrielResponse>builder()
                .message(message)
                .data(response)
                .build();
    }



    public ApiResponse<DeleteFileResponse> removeFileFromCourriel(String courrielNumber, String filename) {
        Courriel courriel = courrielRepository.findByCourrielNumberWithFiles(courrielNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Courriel not found: " + courrielNumber));

        File fileToRemove = courriel.getCourrielFiles().stream()
                .filter(f -> f.getFileName().equalsIgnoreCase(filename))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + filename + " in DB"));

        Path filePath = Paths.get(fileToRemove.getFilePath());

        // Check if file physiquelly exist
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file on disk: " + filename, e);
            }
        } else {
            System.out.println("Warning: file not found on disk, deleting only from DB: " + filePath);
        }

        // delete all attached files
        courriel.getCourrielFiles().remove(fileToRemove);
        courrielRepository.save(courriel);

        DeleteFileResponse response = DeleteFileResponse.builder()
                .courrielNumber(courriel.getCourrielNumber())
                .courrielPath(courriel.getCourrielPath())
                .fileName(fileToRemove.getFileName())
                .build();

        return new ApiResponse<>(true,"File removed successfully", response);
    }



    @Transactional
    public void cleanOrphanFilesFromDB() {

        List<Courriel> allCourriels = courrielRepository.findAllWithFiles();


        for (Courriel courriel : allCourriels) {
            Iterator<File> iterator = courriel.getCourrielFiles().iterator();

            while (iterator.hasNext()) {
                File file = iterator.next();
                Path filePath = Paths.get(file.getFilePath());

                if (!Files.exists(filePath)) {
                    System.out.println("Deleting orphan file record: " + file.getFileName());
                    iterator.remove();
                }
            }

            courrielRepository.save(courriel);
        }
    }

    @Transactional
    public void cleanOrphanCourrielsFromDB() {
        List<Courriel> allCourriels = courrielRepository.findAll();

        for (Courriel courriel : allCourriels) {
            Path courrielDir = Paths.get(courriel.getCourrielPath());

            if (!Files.exists(courrielDir)) {
                System.out.println("Deleting orphan courriel: " + courriel.getCourrielNumber());
                courrielRepository.delete(courriel);
            }
        }
    }


    public void cleanDatabaseFromMissingDiskData() {
        cleanOrphanFilesFromDB();
        cleanOrphanCourrielsFromDB();
    }


    private long convertSizeToBytes(String size) {
        size = size.toUpperCase().trim();
        if (size.endsWith("KB")) return Long.parseLong(size.replace("KB", "")) * 1024;
        if (size.endsWith("MB")) return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        if (size.endsWith("GB")) return Long.parseLong(size.replace("GB", "")) * 1024 * 1024 * 1024;
        throw new IllegalArgumentException("Invalid size format: " + size);
    }


    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
    }

