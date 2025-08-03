package com.API.Documents_Management.Courriel.Controllers;

import com.API.Documents_Management.Courriel.Dto.CreateCourrielRequest;
import com.API.Documents_Management.Courriel.Repos.CourrielDestinationRepo;
import com.API.Documents_Management.Courriel.Repos.CourrielRepo;
import com.API.Documents_Management.Courriel.Services.CourrielService;
import com.API.Documents_Management.Dto.*;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Exceptions.AlreadyExistsException;
import com.API.Documents_Management.Services.AppUserService;
import com.API.Documents_Management.Utils.UserUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/courriels")
@RequiredArgsConstructor
public class CourrielController {

    private final CourrielService courrielService;
    private final AppUserService appUserService;
    private final CourrielRepo courrielRepo;
    private final CourrielDestinationRepo courrielDestinationRepo;

    //===================== Filter ===============================================

//    @PostMapping("/filter")
//    public ResponseEntity<Page<Courriel>> filterCourriels(
//            @RequestBody CourrielFilterRequest filterRequest,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        Page<Courriel> result = courrielService.filterCourriels(filterRequest, page, size);
//        return ResponseEntity.ok(result);
//    }

    //===================== Create ===============================================

    @PreAuthorize("hasAuthority('ADMIN_READ')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateCourrielResponse>> createCourriel(
            @RequestPart("data") CreateCourrielRequest request,
            @RequestPart("files") MultipartFile[] files
    ) {
        AppUser currentUser = UserUtil.getAuthenticatedUser();


        CreateCourrielRequest enrichedRequest = CreateCourrielRequest.builder()
                .courrielNumber(request.courrielNumber())
                .subject(request.subject())
                .description(request.description())
                .courrielType(request.courrielType())
                .nature(request.nature())
                .destinations(request.destinations())
                .sentDate(request.sentDate())
                .arrivedDate(request.arrivedDate())
                .returnDate(request.returnDate())
                .files(files)
                .build();

        try {
            ApiResponse<CreateCourrielResponse> response = courrielService.createCourriel(enrichedRequest, currentUser);
            return ResponseEntity.ok(response);

        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur de traitement de fichier : " + e.getMessage(), null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur inattendue : " + e.getMessage(), null));
        }
    }


    //===================== GET Courriels  ===============================================
//    public ApiResponse<List<CourrielResponseDto>> getCourrielsForUser(AppUser currentUser) {
//        Division division = currentUser.getDivision();
//        Direction direction = currentUser.getDirection();
//        SousDirection sousDirection = currentUser.getSousDirection();
//
////        AlgerianMinistry external = currentUser.getExternal();
//
//        // 📤 Courriels envoyés par la structure de l'utilisateur
//        List<Courriel> sentCourriels = courrielRepo.findByFromDivisionOrFromDirectionOrFromSousDirection(
//                division, direction, sousDirection
//        );
//
//        // 📥 Courriels reçus par la structure (via destinations)
//        List<CourrielDestination> receivedDestinations = courrielDestinationRepo.findAllByToDivisionAndToDirectionAndToSousDirectionAndToExternal(
//                division, direction, sousDirection, external
//        );
//        List<Courriel> receivedCourriels = receivedDestinations.stream()
//                .map(CourrielDestination::getCourriel)
//                .collect(Collectors.toList());
//
//        // 🔁 Fusionner les deux listes, sans doublons
//        Set<Courriel> allCourriels = new HashSet<>();
//        allCourriels.addAll(sentCourriels);
//        allCourriels.addAll(receivedCourriels);
//
//        // Mapper vers DTO
//        List<CourrielResponse> responses = allCourriels.stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//
//        return new ApiResponse<>(true, "Courriels associés à votre structure", responses);
//    }
//


@DeleteMapping
public ResponseEntity<ApiResponse<DeleteCourrielResponse>> deleteCourriel(
        @RequestParam("courrielNumber") String courrielNumber) {

    AppUser currentUser = UserUtil.getAuthenticatedUser();
    ApiResponse<DeleteCourrielResponse> response = courrielService.deleteCourrielByNumber(courrielNumber, currentUser);
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