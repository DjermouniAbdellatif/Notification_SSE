package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Dto.ApiResponse;
import com.API.Documents_Management.Dto.AssignAuthorityRequest;
import com.API.Documents_Management.Entities.Authority;
import com.API.Documents_Management.Exceptions.AuthorityAlreadyExistsException;
import com.API.Documents_Management.Exceptions.InvalidAuthorityNameException;
import com.API.Documents_Management.Exceptions.InvalidRoleNameException;
import com.API.Documents_Management.Services.AuthorityService;
import com.API.Documents_Management.Services.RoleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authorities")
public class AuthorityController {

    @Autowired
    private  AuthorityService authorityService;

    @Autowired
    private RoleService roleService;

    public AuthorityController(AuthorityService authorityService, RoleService roleService) {
        this.authorityService = authorityService;
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Authority>> addAuthority(@RequestBody String authorityName) {
        try {
            Authority authority = authorityService.addAuthority(authorityName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Authority created successfully", authority));
        } catch (AuthorityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (InvalidAuthorityNameException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }


    @PostMapping("/add/list")
    public ResponseEntity<ApiResponse<List<Authority>>> addAuthorities(@RequestBody List<String> authorityNames) {
        List<Authority> added = authorityService.addListOfAuthorities(authorityNames);

        if (added.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false,
                            "Aucune autorité valide n'a été ajoutée.",
                            null));
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Autorités ajoutées avec succès.",
                        added));
    }

    @GetMapping("/name")
    public ResponseEntity<ApiResponse<Authority>> getAuthorityByName(@RequestBody String name) {
        try {
            Authority authority = authorityService.getAuthorityByName(name);
            return ResponseEntity.ok(new ApiResponse<>(true, "Authority found", authority));

            // check if authority name is valid
        } catch (InvalidAuthorityNameException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));


        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Authority>> getAuthorityById(@PathVariable Long id) {
        try {
            Authority authority = authorityService.getAuthorityById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Authority found", authority));
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/name")
    public ResponseEntity<ApiResponse<Authority>> deleteByName(@RequestBody String name) {
        try {
            Authority deleted = authorityService.deleteAuthorityByName(name);
            return ResponseEntity.ok(new ApiResponse<>(true,
                    "Authority with id :"+deleted.getName()+ " deleted successfully",
                    deleted));
        } catch (InvalidAuthorityNameException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Authority>> deleteById(@PathVariable Long id) {
        try {
            Authority deleted = authorityService.deleteAuthorityById(id);
            return ResponseEntity.ok(new ApiResponse<>(true,
                    "Authority with id :"+deleted.getId()+ " deleted successfully",
                    deleted));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Authority>>> getAllAuthorities() {
        List<Authority> authorities = authorityService.getAllAuthorities();

        if (authorities.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(true, "No authority founded", List.of()));
        }

        return ResponseEntity
                .ok(new ApiResponse<>(true, "Authorities founded  successfully", authorities));
    }



}