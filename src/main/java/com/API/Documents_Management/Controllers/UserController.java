package com.API.Documents_Management.Controllers;


import com.API.Documents_Management.Dto.*;
import com.API.Documents_Management.Entities.AppUser;

import com.API.Documents_Management.Exceptions.RoleNotFoundException;
import com.API.Documents_Management.Exceptions.UserAlreadyExistsException;
import com.API.Documents_Management.Exceptions.UserNotFoundException;
import com.API.Documents_Management.Services.AppUserService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;




@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired private AppUserService appUserService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<ApiResponse<GetUserDTO>> addUser(@RequestBody UserRequest appUser) {
        AppUser userToAdd = new AppUser();
        userToAdd.setUsername(appUser.getUsername());
        userToAdd.setPassword(passwordEncoder.encode(appUser.getPassword()));
        userToAdd.setRoles(new HashSet<>());

        GetUserDTO savedUser = appUserService.addAppUser(userToAdd);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "User created", savedUser));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GetUserDTO>> getAllUsers() {
        return ResponseEntity.ok(appUserService.findAllAppUsers());
    }

    @GetMapping("/username")
    public ResponseEntity<GetUserDTO> getUserByUsername(@RequestBody String username) {
        return ResponseEntity.ok(appUserService.findByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.findAppUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<GetUserDTO>> deleteUserById(@PathVariable Long id) {
        GetUserDTO deleted = appUserService.deleteAppUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted", deleted));
    }

    @DeleteMapping("/username")
    public ResponseEntity<ApiResponse<GetUserDTO>> deleteUserByUsername(@RequestBody String username) {
        GetUserDTO deleted = appUserService.deleteAppUserByUsername(username);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted", deleted));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GetUserDTO>> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest update) {
        GetUserDTO updated = appUserService.updateAppUser(id, update);
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated", updated));
    }

    @PostMapping("/role/assign")
    public ResponseEntity<ApiResponse<GetUserDTO>> assignRole(@RequestBody AssignRoleRequest request) {
        GetUserDTO updated = appUserService.assignRoleToUser(request.getUsername(), request.getRoleName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Role assigned", updated));
    }

    @DeleteMapping("/role/remove")
    public ResponseEntity<ApiResponse<GetUserDTO>> removeRole(@RequestBody AssignRoleRequest request) {
        GetUserDTO updated = appUserService.deleteRoleFromUser(request.getUsername(), request.getRoleName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Role removed", updated));
    }

    @GetMapping("/role")
    public ResponseEntity<List<GetUserDTO>> getUsersByRole(@RequestBody String roleName) {
        return ResponseEntity.ok(appUserService.getUsersByRole(roleName));
    }
}
