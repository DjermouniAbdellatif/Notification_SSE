package com.API.Documents_Management.Courriel.Services;


import com.API.Documents_Management.Courriel.Entities.Structure;
import com.API.Documents_Management.Courriel.Enums.CourrielType;
import com.API.Documents_Management.Courriel.Dto.CreateCourrielRequest;
import com.API.Documents_Management.Courriel.Entities.Courriel;
import com.API.Documents_Management.Courriel.Entities.CourrielDestination;
import com.API.Documents_Management.Courriel.Entities.File;
import com.API.Documents_Management.Courriel.Enums.NatureCourriel;
import com.API.Documents_Management.Courriel.Enums.PosteUser;
import com.API.Documents_Management.Courriel.Repos.CourrielDestinationRepo;
import com.API.Documents_Management.Courriel.Repos.CourrielRepo;
import com.API.Documents_Management.Courriel.Repos.FileRepo;
import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionRepo;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Division.DivisionRepo;
import com.API.Documents_Management.Dto.*;
import com.API.Documents_Management.Enums.HierarchyLevel;
import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Exceptions.*;
import com.API.Documents_Management.Entities.*;

import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.SousDirection.SousDierctionRepo;
import com.API.Documents_Management.SousDirection.SousDirection;
import com.API.Documents_Management.Utils.FormatUtils;
import com.API.Documents_Management.Notification.Services.NotificationService;
import com.API.Documents_Management.Utils.UserUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;


@Slf4j
@Service
public class CourrielService {

    private final NotificationService notificationService;
    private final CourrielDestinationRepo courrielDestinationRepo;
    private final CourrielRepo courrielRepo;
    private final FileRepo fileRepository;
    private final String basePath;
    private final long maxFileSize;
    private final long totalMaxSize;
    private final DivisionRepo divisionRepo;
    private final DirectionRepo directionRepo;
    private final SousDierctionRepo sousDierctionRepo;
    private final AppUserRepo appUserRepo;
    private final UserUtil userUtil;

    public CourrielService(
            NotificationService notificationService,
            CourrielDestinationRepo courrielDestinationRepo, CourrielRepo courrielRepo,
            FileRepo fileRepository,
            @Value("${file.storagePath}") String basePath,
            @Value("${file.maxSize}") String maxFileSize,
            @Value("${file.totalMaxSize}") String totalMaxSize,
            DivisionRepo divisionRepo, DirectionRepo directionRepo, SousDierctionRepo sousDierctionRepo, AppUserRepo appUserRepo, UserUtil userUtil) {
        this.notificationService = notificationService;
        this.courrielDestinationRepo = courrielDestinationRepo;
        this.courrielRepo = courrielRepo;
        this.fileRepository = fileRepository;
        this.basePath = basePath;
        this.maxFileSize = convertSizeToBytes(maxFileSize);
        this.totalMaxSize = convertSizeToBytes(totalMaxSize);
        this.divisionRepo = divisionRepo;
        this.directionRepo = directionRepo;
        this.sousDierctionRepo = sousDierctionRepo;
        this.appUserRepo = appUserRepo;
        this.userUtil = userUtil;
    }

    public String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "anonymous";
    }




    //===================== Check Duplication    ==========================================

    public boolean existsInCurrentUserStructure(String courrielNumber, CourrielType type, AppUser currentUser) {
        Structure structure = userUtil.getStructure(currentUser);

        return switch (structure.getTypeStructure()) {
            case DIVISION -> {
                Division division = (Division) structure;
                yield type == CourrielType.DEPART
                        ? courrielRepo.existsByCourrielNumberAndCourrielTypeAndFromDivision_Id(
                        courrielNumber, type, division.getId())
                        : courrielRepo.existsByCourrielNumberAndCourrielTypeAndToDivision(
                        courrielNumber, type, division.getId());
            }
            case DIRECTION -> {
                Direction direction = (Direction) structure;
                yield type == CourrielType.DEPART
                        ? courrielRepo.existsByCourrielNumberAndCourrielTypeAndFromDirection_Id(
                        courrielNumber, type, direction.getId())
                        : courrielRepo.existsByCourrielNumberAndCourrielTypeAndToDirection(
                        courrielNumber, type, direction.getId());
            }
            case SOUS_DIRECTION -> {
                SousDirection sousDirection = (SousDirection) structure;
                yield type == CourrielType.DEPART
                        ? courrielRepo.existsByCourrielNumberAndCourrielTypeAndFromSousDirection_Id(
                        courrielNumber, type, sousDirection.getId())
                        : courrielRepo.existsByCourrielNumberAndCourrielTypeAndToSousDirection(
                        courrielNumber, type, sousDirection.getId());
            }
            default -> false;
        };
    }

    //===================== Create  ==========================================

    @Transactional
    public ApiResponse<CreateCourrielResponse> createCourriel(CreateCourrielRequest request, AppUser currentUser) throws IOException {
        // Conversion du type
        CourrielType type = CourrielType.valueOf(request.courrielType().trim().toUpperCase());

        // === Vérification des doublons ===
        if (existsInCurrentUserStructure(request.courrielNumber(), type, currentUser)) {
            Structure structure = userUtil.getStructure(currentUser);
            String structureType = type == CourrielType.DEPART ? "d'expédition" : "de réception";
            throw new AlreadyExistsException(String.format(
                    "Un courriel %s avec le numéro %s existe déjà dans votre structure %s (%s)",
                    type,
                    request.courrielNumber(),
                    structure.getTypeStructure(),
                    userUtil.getStructureName(structure)
            ));
        }

        // === Traitement des fichiers ===
        List<MultipartFile> uploadedFiles = List.of(request.files());
        List<SkippedFileError> skippedFiles = new ArrayList<>();
        List<MultipartFile> validFiles = filterValidFiles(uploadedFiles, skippedFiles);

        if (validFiles.isEmpty()) {
            return new ApiResponse<>(false, "Aucun fichier valide à traiter", null);
        }

        // === Création du dossier de stockage ===
        String folderName = generateFolderName(request.courrielNumber(), currentUser.getId());
        Path folderPath = Paths.get(basePath, folderName);
        Files.createDirectories(folderPath);

        // === Compression & sauvegarde des fichiers ===
        Set<File> courrielFiles = new HashSet<>();
        List<UploadFileResponse> uploadedFileResponses = compressAndStoreFiles(validFiles, folderPath, courrielFiles, skippedFiles);

        if (courrielFiles.isEmpty()) {
            Files.deleteIfExists(folderPath);
            return new ApiResponse<>(false, "Tous les fichiers ont échoué à la compression.", null);
        }

        // === Construction de l'entité Courriel ===
        Courriel courriel = buildCourrielEntity(request, currentUser, folderPath, courrielFiles);
        setCourrielStructures(courriel, request, currentUser);

        // === Enregistrement ===
        courrielRepo.save(courriel);

        // === Envoi de notification ===
        sendCreationNotification(request, currentUser, uploadedFileResponses);

        return buildSuccessResponse(request, uploadedFileResponses, skippedFiles);
    }



    //===================== Download File  ==========================================

    public ResponseEntity<Resource> downloadFile(String courrielNumber, String fileName) {

        // Get courriel data from DB
        Courriel courriel = courrielRepo.findByCourrielNumberWithFiles(courrielNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Courriel not found: " + courrielNumber));

        // Check if file exist
        com.API.Documents_Management.Courriel.Entities.File file = courriel.getCourrielFiles().stream()
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


    //===================== Delete courriel ==========================================

    @Transactional
    public ApiResponse<DeleteCourrielResponse> deleteCourrielByNumber(String courrielNumber, AppUser currentUser) {
return null;
    }




    //===================== Add File to courriel ==========================================

    public ApiResponse<CreateCourrielResponse> addFilesToCourriel(
            String courrielNumber,
            List<MultipartFile> filesToSave
    ) throws IOException {

        // get authentified user
        String user=getUser();

        if (filesToSave == null || filesToSave.isEmpty()) {
            throw new EmptyFileException("No files provided.");
        }

        Courriel courriel = courrielRepo
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

            com.API.Documents_Management.Courriel.Entities.File fileEntity = com.API.Documents_Management.Courriel.Entities.File.builder()
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

        courrielRepo.save(courriel);

        // 📢 Notification  WebSocket

        List<String> filesNames=uploadedFiles.stream().map(n->n.fileName()).collect(Collectors.toList());

        notificationService.sendNotification("Courriel n° " + courrielNumber+" a été modifier",courrielNumber,CleaningFilesNames(filesNames), Operations.UPLOAD_FILE, user);



        CreateCourrielResponse response = CreateCourrielResponse.builder()
                .courrielNumber(courrielNumber)
                .uploadedFiles(uploadedFiles)
                .skippedFiles(skippedFiles)
                .build();

        String message="Courriel : "+courrielNumber+" Updated.";
        Boolean isSucces = true;

        if(uploadedFiles.isEmpty()) {
            message="Courriel Not Updated !";
            isSucces = false;
        }

        return ApiResponse.<CreateCourrielResponse>builder()
                .isSucces(isSucces)
                .message(message)
                .data(response)
                .build();
    }


    //===================== Remove File from courriel ==========================================

    public ApiResponse<DeleteFileResponse> removeFileFromCourriel(String courrielNumber, String filename) {


        // get authentified user
        String user=getUser();

        Courriel courriel = courrielRepo.findByCourrielNumberWithFiles(courrielNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Courriel not found: " + courrielNumber));

        com.API.Documents_Management.Courriel.Entities.File fileToRemove = courriel.getCourrielFiles().stream()
                .filter(f -> f.getFileName().equalsIgnoreCase(filename))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + filename + " in DB"));

        Path filePath = Paths.get(fileToRemove.getFilePath());

        boolean isSuccess = true;

        // Check if file physiquelly exist
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file on disk: " + filename, e);
            }
        } else {
            System.out.println("Warning: file not found on disk, deleting only from DB: " + filePath);
            isSuccess = false;
        }

        // delete all attached files
        courriel.getCourrielFiles().remove(fileToRemove);
        courrielRepo.save(courriel);


        // 📢 Notification  WebSocket

        List<String> filesNames=List.of(filename);

        notificationService.sendNotification("Courriel n° " + courrielNumber+" a été modifier",courrielNumber,CleaningFilesNames(filesNames), Operations.DELETE_FILE, user);



        DeleteFileResponse response = DeleteFileResponse.builder()
                .courrielNumber(courriel.getCourrielNumber())
                .courrielPath(courriel.getCourrielPath())
                .fileName(fileToRemove.getFileName())
                .build();

        return new ApiResponse<>(isSuccess,"File removed successfully", response);
    }


    //====================== Courriel Helper Methode==================================================

    private void sendCreationNotification(CreateCourrielRequest request, AppUser currentUser,
                                          List<UploadFileResponse> uploadedFileResponses) {
        notificationService.sendNotification(
                "Nouveau courriel " + request.courrielNumber() + " a été créé.",
                request.courrielNumber(),
                uploadedFileResponses.stream().map(UploadFileResponse::fileName).collect(Collectors.toSet()),
                Operations.CREATE,
                currentUser.getUsername()
        );
    }

    private ApiResponse<CreateCourrielResponse> buildSuccessResponse(CreateCourrielRequest request,
                                                                     List<UploadFileResponse> uploadedFileResponses,
                                                                     List<SkippedFileError> skippedFiles) {
        return new ApiResponse<>(true, "Courriel créé avec succès", CreateCourrielResponse.builder()
                .courrielNumber(request.courrielNumber())
                .uploadedFiles(uploadedFileResponses)
                .skippedFiles(skippedFiles)
                .build());
    }

    private void setCourrielStructures(Courriel courriel, CreateCourrielRequest request, AppUser currentUser) {
        CourrielType type = courriel.getCourrielType();

        if (type == CourrielType.DEPART) {
            setDepartStructures(courriel, currentUser);
            setDestinations(courriel, request);
        } else {
            setArriverStructures(courriel, currentUser);
        }
    }

    private void setDepartStructures(Courriel courriel, AppUser currentUser) {
        courriel.setFromDivision(currentUser.getDivision());
        courriel.setFromDirection(currentUser.getDirection());
        courriel.setFromSousDirection(currentUser.getSousDirection());
    }

    private void setDestinations(Courriel courriel, CreateCourrielRequest request) {
        List<CourrielDestination> destinations = request.destinations().stream()
                .map(dest -> CourrielDestination.builder()
                        .courriel(courriel)
                        .toDivision(getDivision(dest.toDivisionID()))
                        .toDirection(getDirection(dest.toDirectionID()))
                        .toSousDirection(getSousDirection(dest.toSousDirectionID()))
                        .build())
                .toList();
        courriel.setDestinations(destinations);
    }

    private void setArriverStructures(Courriel courriel, AppUser currentUser) {
        courriel.setDestinations(List.of(
                CourrielDestination.builder()
                        .courriel(courriel)
                        .toDivision(currentUser.getDivision())
                        .toDirection(currentUser.getDirection())
                        .toSousDirection(currentUser.getSousDirection())
                        .build()
        ));
    }

    private List<MultipartFile> filterValidFiles(List<MultipartFile> uploadedFiles, List<SkippedFileError> skippedFiles) {
        List<MultipartFile> validFiles = new ArrayList<>();
        long totalSize = 0;

        for (MultipartFile file : uploadedFiles) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();

            if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
                skippedFiles.add(new SkippedFileError(originalName, "Seuls les fichiers PDF sont autorisés"));
                continue;
            }

            if (file.getSize() > maxFileSize) {
                skippedFiles.add(new SkippedFileError(originalName, "Fichier trop volumineux (max 10 MB)"));
                continue;
            }

            totalSize += file.getSize();
            if (totalSize > totalMaxSize) {
                skippedFiles.add(new SkippedFileError(originalName, "Taille totale dépassée (max 100 MB)"));
                break;
            }

            validFiles.add(file);
        }

        return validFiles;
    }

    private String generateFolderName(String courrielNumber, Long userId) {
        return courrielNumber + "_" + userId + "_" + UUID.randomUUID().toString().substring(0, 10);
    }

    private List<UploadFileResponse> compressAndStoreFiles(List<MultipartFile> files, Path folderPath,
                                                           Set<File> courrielFiles, List<SkippedFileError> skippedFiles) throws IOException {
        List<UploadFileResponse> responses = new ArrayList<>();

        for (MultipartFile multipartFile : files) {
            String originalName = Paths.get(multipartFile.getOriginalFilename()).getFileName().toString();
            String compressedFileName = originalName + ".gz";
            Path compressedPath = folderPath.resolve(compressedFileName);

            try (
                    InputStream input = new BufferedInputStream(multipartFile.getInputStream());
                    OutputStream output = new GZIPOutputStream(Files.newOutputStream(compressedPath))
            ) {
                input.transferTo(output);
            } catch (IOException e) {
                skippedFiles.add(new SkippedFileError(originalName, "Erreur de compression"));
                continue;
            }

            long size = Files.size(compressedPath);

            courrielFiles.add(File.builder()
                    .fileName(compressedFileName)
                    .filePath(compressedPath.toString())
                    .fileSize(size)
                    .fileType("application/pdf")
                    .build());

            responses.add(UploadFileResponse.builder()
                    .fileName(compressedFileName)
                    .filePath(compressedPath.toString())
                    .fileSize(FormatUtils.formatFileSize(size))
                    .build());
        }

        return responses;
    }

    private Courriel buildCourrielEntity(CreateCourrielRequest request, AppUser user, Path folderPath, Set<File> files) {
        return Courriel.builder()
                .createdBy(user.getUsername())
                .courrielNumber(request.courrielNumber())
                .courrielType(CourrielType.valueOf(request.courrielType().trim().toUpperCase()))
                .nature(NatureCourriel.valueOf(request.nature().trim().toUpperCase()))
                .courrielPath(folderPath.toString())
                .sentDate(request.sentDate())
                .arrivedDate(request.arrivedDate())
                .saveDate(LocalDateTime.now())
                .courrielFiles(files)
                .build();
    }


    private Division getDivision(Long id) {
        return id != null ? divisionRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Division introuvable")) : null;
    }

    private Direction getDirection(Long id) {
        return id != null ? directionRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Direction introuvable")) : null;
    }

    private SousDirection getSousDirection(Long id) {
        return id != null ? sousDierctionRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Sous-direction introuvable")) : null;
    }



    // ========================= DB  cleanup methodes ====================================




    @Transactional
    public void cleanOrphanFilesFromDB() {

        List<Courriel> allCourriels = courrielRepo.findAllWithFiles();


        for (Courriel courriel : allCourriels) {
            Iterator<com.API.Documents_Management.Courriel.Entities.File> iterator = courriel.getCourrielFiles().iterator();

            while (iterator.hasNext()) {
                File file = iterator.next();
                Path filePath = Paths.get(file.getFilePath());

                if (!Files.exists(filePath)) {
                    System.out.println("Deleting orphan file record: " + file.getFileName());
                    iterator.remove();
                }
            }

            courrielRepo.save(courriel);
        }
    }



    private Path generateCourrielPath(String courrielNumber, AppUser user) {
        String baseName = sanitize(courrielNumber) + "_" + user.getId() + "_" + generateShortUUID();
        return Paths.get(basePath, baseName);
    }

    private String generateShortUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
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




    private Set<String> CleaningFilesNames(List<String> uploadedFiles) {

        return  uploadedFiles.stream()
                .map(name -> name.replaceFirst("\\.gz$", "")) // ✅ Delete ".gz"
                .collect(Collectors.toSet());
    }


    @Transactional
    public void cleanOrphanCourrielsFromDB() {
        List<Courriel> allCourriels = courrielRepo.findAll();

        for (Courriel courriel : allCourriels) {
            Path courrielDir = Paths.get(courriel.getCourrielPath());

            if (!Files.exists(courrielDir)) {
                System.out.println("Deleting orphan courriel: " + courriel.getCourrielNumber());
                courrielRepo.delete(courriel);
            }
        }
    }



}


