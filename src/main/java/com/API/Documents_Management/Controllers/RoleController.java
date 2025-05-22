package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Dto.AssignAuthorityRequest;
import com.API.Documents_Management.Dto.RoleListRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Dto.ApiResponse;
import com.API.Documents_Management.Services.RoleService;
import com.API.Documents_Management.Exceptions.RoleAlreadyExistsException;
import com.API.Documents_Management.Exceptions.InvalidRoleNameException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> addRole(@RequestBody String name) {
        try {
            Role role = roleService.addRole(name);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Role created successfully", role));
        } catch (RoleAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (InvalidRoleNameException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/addList")
    public ResponseEntity<ApiResponse<List<Role>>> addListOfRoles(@RequestBody RoleListRequest requestuest) {
        List<Role> createdRoles = roleService.addListOfRoles(requestuest.getRoles());

        if (createdRoles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "No valid roles added.", null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Roles created successfully", createdRoles));
    }

    @GetMapping("/name")
    public ResponseEntity<ApiResponse<Role>> getRoleByName(@RequestBody String name) {
        try {
            Role role = roleService.getRoleByName(name);
            return ResponseEntity.ok(new ApiResponse<>(true, "Role found", role));
        } catch (EntityNotFoundException |InvalidRoleNameException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        try {
            Role role = roleService.getRoleById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Role found", role));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/name")
    public ResponseEntity<ApiResponse<Role>> deleteRoleByName(@RequestBody String name) {
        try {
            Role deletedRole = roleService.deleteRoleByName(name);
            return ResponseEntity.ok(new ApiResponse<>(true, "Role deleted successfully", deletedRole));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> deleteRoleById(@PathVariable Long id) {
        try {
            Role role = roleService.getRoleById(id);
            Role deletedRole = roleService.deleteRoleByName(role.getName().name());
            return ResponseEntity.ok(new ApiResponse<>(true, "Role deleted successfully", deletedRole));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> allRoles = roleService.getAllRoles();
        return ResponseEntity.ok(new ApiResponse<>(true, "Roles found", allRoles));
    }


@PostMapping("/authority/assign")
public ResponseEntity<ApiResponse<Void>> assignAuthorityToRole(
        @Valid @RequestBody AssignAuthorityRequest req) {
    try {
        // Check if role existe or assign it
        roleService.assignAuthorityToRole(req.getRoleName(), req.getAuthorityName());
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                String.format("Authority '%s' assigned to role '%s'",
                        req.getAuthorityName(), req.getRoleName()),
                null
        ));
    } catch (InvalidRoleNameException | EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, e.getMessage(), null));
    } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }
}

    @DeleteMapping("/authority/remove")
    public ResponseEntity<ApiResponse<Void>> deleteAuthorityFromRole(
            @Valid @RequestBody AssignAuthorityRequest req) {
        try {
            // Check if already deleted
            roleService.deleteAuthorityFromRole(req.getRoleName(), req.getAuthorityName());
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    String.format("Authority '%s' removed from role '%s'",
                            req.getAuthorityName(), req.getRoleName()),
                    null
            ));
        } catch (InvalidRoleNameException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

}
