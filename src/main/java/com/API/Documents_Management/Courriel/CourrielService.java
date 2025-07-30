package com.API.Documents_Management.Courriel;


import com.API.Documents_Management.Courriel.Dto.CreateCourrielRequest;
import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionRepo;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Division.DivisionRepo;
import com.API.Documents_Management.Dto.*;
import com.API.Documents_Management.Enums.Operations;
import com.API.Documents_Management.Exceptions.*;
import com.API.Documents_Management.Entities.*;

import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.SousDirection.SousDierctionRepo;
import com.API.Documents_Management.SousDirection.SousDirection;
import com.API.Documents_Management.Utils.FormatUtils;
import com.API.Documents_Management.Notification.Services.NotificationService;
import jakarta.persistence.EntityNotFoundException;
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

    public CourrielService(
            NotificationService notificationService,
            CourrielDestinationRepo courrielDestinationRepo, CourrielRepo courrielRepo,
            FileRepo fileRepository,
            @Value("${file.storagePath}") String basePath,
            @Value("${file.maxSize}") String maxFileSize,
            @Value("${file.totalMaxSize}") String totalMaxSize,
            DivisionRepo divisionRepo, DirectionRepo directionRepo, SousDierctionRepo sousDierctionRepo, AppUserRepo appUserRepo) {
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
    }

    public String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "anonymous";
    }




    //===================== Check Duplication in destination   ==========================================

    public boolean isDuplicate(String courrielNumber, AppUser currentUser) {
        Division division = currentUser.getDivision();
        Direction direction = currentUser.getDirection();
        SousDirection sousDirection = currentUser.getSousDirection();

        // Cas 1 : Chef de division
        if (division != null && direction == null && sousDirection == null) {
            return courrielDestinationRepo.existsByCourriel_CourrielNumberAndToDivision_IdAndToDirectionIsNullAndToSousDirectionIsNull(
                    courrielNumber, division.getId());
        }

        // Cas 2 : Directeur
        if (division != null && direction != null && sousDirection == null) {
            return courrielDestinationRepo.existsByCourriel_CourrielNumberAndToDirection_IdAndToSousDirectionIsNull(
                    courrielNumber, direction.getId());
        }

        // Cas 3 : Sous-directeur
        if (division != null && direction != null && sousDirection != null) {
            return courrielDestinationRepo.existsByCourriel_CourrielNumberAndToSousDirection_Id(
                    courrielNumber, sousDirection.getId());
        }

        return false;
    }





    //===================== Create  ==========================================

    @Transactional
    public ApiResponse<CreateCourrielResponse> createCourriel(CreateCourrielRequest request, AppUser currentUser) throws IOException {

        if (isDuplicate(request.courrielNumber(), currentUser)) {
            throw new AlreadyExistsException("Un courriel avec le numéro " + request.courrielNumber() + " existe déjà !");
        }

        // Validation des fichiers
        List<MultipartFile> uploadedFiles = List.of(request.files());
        List<SkippedFileError> skippedFiles = new ArrayList<>();
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

        if (validFiles.isEmpty()) {
            return new ApiResponse<>(false, "Aucun fichier valide à traiter", null);
        }

        // Création du dossier
        String folderName = request.courrielNumber() + "_" + currentUser.getId() + "_" + UUID.randomUUID().toString().substring(0, 10);
        Path folderPath = Paths.get(basePath, folderName);
        Files.createDirectories(folderPath);

        // Compression et enregistrement des fichiers
        Set<File> courrielFiles = new HashSet<>();
        List<UploadFileResponse> uploadedFileResponses = new ArrayList<>();

        for (MultipartFile multipartFile : validFiles) {
            String originalName = Paths.get(multipartFile.getOriginalFilename()).getFileName().toString();
            String compressedFileName = originalName + ".gz";
            Path compressedFilePath = folderPath.resolve(compressedFileName);

            try (
                    InputStream input = new BufferedInputStream(multipartFile.getInputStream());
                    OutputStream output = new GZIPOutputStream(Files.newOutputStream(compressedFilePath))
            ) {
                input.transferTo(output);
            } catch (IOException e) {
                skippedFiles.add(new SkippedFileError(originalName, "Erreur de compression"));
                continue;
            }

            long size = Files.size(compressedFilePath);

            courrielFiles.add(File.builder()
                    .fileName(compressedFileName)
                    .filePath(compressedFilePath.toString())
                    .fileSize(size)
                    .fileType("application/pdf")
                    .build());

            uploadedFileResponses.add(UploadFileResponse.builder()
                    .fileName(compressedFileName)
                    .filePath(compressedFilePath.toString())
                    .fileSize(FormatUtils.formatFileSize(size))
                    .build());
        }

        if (courrielFiles.isEmpty()) {
            Files.deleteIfExists(folderPath);
            return new ApiResponse<>(false, "Tous les fichiers ont échoué à la compression.", null);
        }

        CourrielType type = CourrielType.valueOf(request.courrielType().trim().toUpperCase());
        NatureCourriel nature = NatureCourriel.valueOf(request.nature().trim().toUpperCase());

        Courriel courriel = Courriel.builder()
                .createdBy(currentUser.getUsername())
                .courrielNumber(request.courrielNumber())
                .courrielType(type)
                .nature(nature)
                .courrielPath(folderPath.toString())
                .sentDate(request.sentDate())
                .arrivedDate(request.arrivedDate())
                .saveDate(LocalDateTime.now())
                .courrielFiles(courrielFiles)
                .build();

        if (type == CourrielType.DEPART) {
            courriel.setFromDivision(currentUser.getDivision());
            courriel.setFromDirection(currentUser.getDirection());
            courriel.setFromSousDirection(currentUser.getSousDirection());

            List<CourrielDestination> destinations = request.destinations().stream()
                    .map(dest -> CourrielDestination.builder()
                            .courriel(courriel)
                            .toDivision(dest.divisionID() != null ? divisionRepo.findById(dest.divisionID()).orElseThrow(() -> new EntityNotFoundException("Division introuvable")) : null)
                            .toDirection(dest.directionID() != null ? directionRepo.findById(dest.directionID()).orElseThrow(() -> new EntityNotFoundException("Direction introuvable")) : null)
                            .toSousDirection(dest.sousDirectionID() != null ? sousDierctionRepo.findById(dest.sousDirectionID()).orElseThrow(() -> new EntityNotFoundException("Sous-direction introuvable")) : null)
                            .build())
                    .toList();

            courriel.setDestinations(destinations);
        } else if (type == CourrielType.ARRIVER) {
            courriel.setDestinations(List.of(
                    CourrielDestination.builder()
                            .courriel(courriel)
                            .toDivision(currentUser.getDivision())
                            .toDirection(currentUser.getDirection())
                            .toSousDirection(currentUser.getSousDirection())
                            .build()
            ));
        }

        courrielRepo.save(courriel);

        notificationService.sendNotification(
                "Nouveau courriel " + request.courrielNumber() + " a été créé.",
                request.courrielNumber(),
                uploadedFileResponses.stream().map(UploadFileResponse::fileName).collect(Collectors.toSet()),
                Operations.CREATE,
                currentUser.getUsername()
        );

        return new ApiResponse<>(true, "Courriel créé avec succès", CreateCourrielResponse.builder()
                .courrielNumber(request.courrielNumber())
                .uploadedFiles(uploadedFileResponses)
                .skippedFiles(skippedFiles)
                .build());
    }




    //===================== Download File  ==========================================

    public ResponseEntity<Resource> downloadFile(String courrielNumber, String fileName) {

        // Get courriel data from DB
        Courriel courriel = courrielRepo.findByCourrielNumberWithFiles(courrielNumber)
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


    //===================== Delete courriel ==========================================

    @Transactional
    public ApiResponse<DeleteCourrielResponse> deleteCourrielByNumber(String courrielNumber, AppUser currentUser) {
        // Récupérer le courriel
        Courriel courriel = courrielRepo.findByCourrielNumberWithFiles(courrielNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Courriel introuvable avec le numéro : " + courrielNumber));

        AppUser creator = appUserRepo.findAppUserByUsername(courriel.getCreatedBy()).orElse(null);

        if (creator == null) {
            throw new IllegalStateException("Impossible de déterminer le créateur du courriel.");
        }

        // Vérification des droits

        // est un simple user
        if ((!currentUser.hasRole("ADMIN"))
                &&((currentUser.hasRole("USER")))
                &&(currentUser.getSousDirection() != null)
        ) {
            throw new AccessDeniedException("Vous n'avez pas l'autorisation en tant que simple utilisateur pour supprimer ce courriel.");
        }

        boolean sameDivision = creator.getDivision() != null &&
                currentUser.getDivision() != null &&
                creator.getDivision().getId().equals(currentUser.getDivision().getId());

        boolean sameDirection = creator.getDirection() != null &&
                currentUser.getDirection() != null &&
                creator.getDirection().getId().equals(currentUser.getDirection().getId());

        boolean isDirecteur = (currentUser.hasRole("ADMIN")&&((currentUser.getSousDirection()!=null)||(currentUser.getDirection()!=null)));

        boolean isChefDivision = (currentUser.hasRole("ADMIN")&&(currentUser.getDivision()!=null));

        boolean canDelete = (isDirecteur && sameDirection) || (isChefDivision && sameDivision);

        if (!canDelete) {
            throw new AccessDeniedException("Vous n'avez pas les droits pour supprimer ce courriel.");
        }

        // Supprimer les fichiers
        Path folderPath = Paths.get(courriel.getCourrielPath());
        try {
            if (Files.exists(folderPath)) {
                Files.walk(folderPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(f -> {
                            if (!f.delete()) {
                                System.err.println("Impossible de supprimer le fichier : " + f.getAbsolutePath());
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression des fichiers du courriel : " + courrielNumber, e);
        }

        // Supprimer en base de données
        courrielRepo.delete(courriel);


        String label = resolveStructureString(currentUser);

        // Notification WebSocket
        notificationService.sendNotification(
                "Le courriel n° " + courrielNumber + " a été supprimé pour votre structure : " + label,
                courrielNumber,
                Set.of(),
                Operations.DELETE,
                currentUser.getUsername()
        );

        // Réponse
        DeleteCourrielResponse response = DeleteCourrielResponse.builder()
                .courrielNumber(courriel.getCourrielNumber())
                .courrielPath(courriel.getCourrielPath())
                .build();

        return ApiResponse.<DeleteCourrielResponse>builder()
                .isSucces(true)
                .message("Courriel supprimé avec succès pour votre structure : " + label)
                .data(response)
                .build();
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

        File fileToRemove = courriel.getCourrielFiles().stream()
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


//=============================== Hierarchy Utils =====================================

    private String resolveStructureString(AppUser user) {

    String name;
    String structure;

    if (user.getSousDirection() != null) {
        structure="Sous Direction";
        name=user.getSousDirection().getName();
    } else if (user.getDirection() != null) {
        structure="Direction";
        name=user.getDirection().getName();
    }else{
        structure="Division";
        name=user.getDivision().getName();
    }

    return structure+" "+name;
}






    @Transactional
    public void cleanOrphanFilesFromDB() {

        List<Courriel> allCourriels = courrielRepo.findAllWithFiles();


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


