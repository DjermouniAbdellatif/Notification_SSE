package com.API.Documents_Management.Services_Impl;

import com.API.Documents_Management.Dto.GetUserDTO;
import com.API.Documents_Management.Dto.UpdateUserRequest;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Enums.RoleType;
import com.API.Documents_Management.Exceptions.UserAlreadyExistsException;
import com.API.Documents_Management.Exceptions.UserNotFoundException;
import com.API.Documents_Management.Exceptions.RoleNotFoundException;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Repositories.RoleRepo;
import com.API.Documents_Management.Repositories.TokenRepo;
import com.API.Documents_Management.Services.AppUserService;
import com.API.Documents_Management.Services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AppUserServiceImpl implements AppUserService {
    @Autowired private AppUserRepo appUserRepo;
    @Autowired private RoleRepo roleRepo;
    @Autowired private TokenRepo tokenRepo;
    @Autowired private RoleService roleService;
    @Autowired private PasswordEncoder passwordEncoder;

    public GetUserDTO addAppUser(AppUser appUser) {
        if (appUserRepo.findAppUserByUsername(appUser.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists: " + appUser.getUsername());
        }
        return GetUserDTO.toDto(appUserRepo.save(appUser));
    }

    public GetUserDTO assignRoleToUser(String username, String roleName) {
        AppUser user = findAppUserByUsername(username);
        Role role = roleService.getRoleByName(roleName);
        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equalsIgnoreCase(roleName));
        if (hasRole) throw new IllegalStateException("User already has role: " + roleName);
        user.getRoles().add(role);
        return GetUserDTO.toDto(appUserRepo.save(user));
    }

    public GetUserDTO deleteRoleFromUser(String username, String roleName) {
        AppUser user = findAppUserByUsername(username);
        Role role = roleService.getRoleByName(roleName);
        user.getRoles().removeIf(r -> r.getName().equals(role.getName()));
        return GetUserDTO.toDto(appUserRepo.save(user));
    }

    public List<GetUserDTO> findAllAppUsers() {
        return appUserRepo.findAll().stream()
                .map(GetUserDTO::toDto)
                .collect(Collectors.toList());
    }

    public GetUserDTO findByUsername(String username) {
        return GetUserDTO.toDto(findAppUserByUsername(username));
    }

    public GetUserDTO findAppUserById(Long id) {
        AppUser user = appUserRepo.findAppUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return GetUserDTO.toDto(user);
    }

    @Transactional
    public GetUserDTO deleteAppUserById(Long id) {
        AppUser user = findAppUserByIdInternal(id);
        tokenRepo.deleteAllByUser(user);
        appUserRepo.delete(user);
        return GetUserDTO.toDto(user);
    }

    @Transactional
    public GetUserDTO deleteAppUserByUsername(String username) {
        AppUser user = findAppUserByUsername(username);
        tokenRepo.deleteAllByUser(user);
        appUserRepo.delete(user);
        return GetUserDTO.toDto(user);
    }

    @Transactional
    public GetUserDTO updateAppUser(Long id, UpdateUserRequest userUpdate) {
        AppUser user = findAppUserByIdInternal(id);
        user.setUsername(userUpdate.getUsername());
        user.setPassword(passwordEncoder.encode(userUpdate.getPassword()));

        if (userUpdate.getRoles() != null) {
            Set<Role> roles = userUpdate.getRoles().stream()
                    .map(roleName -> roleRepo.findByName(RoleType.valueOf(roleName))
                            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return GetUserDTO.toDto(appUserRepo.save(user));
    }

    public List<GetUserDTO> getUsersByRole(String roleName) {
        Role role = roleService.getRoleByName(roleName);
        return appUserRepo.findByRolesContaining(role).stream()
                .map(GetUserDTO::toDto)
                .collect(Collectors.toList());
    }

    private AppUser findAppUserByUsername(String username) {
        return appUserRepo.findAppUsersByUsernameWithRoles(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private AppUser findAppUserByIdInternal(Long id) {
        return appUserRepo.findAppUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
}
