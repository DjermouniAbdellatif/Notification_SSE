package com.API.Documents_Management.Services_Impl;

import com.API.Documents_Management.Entities.Authority;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Enums.RoleType;

import com.API.Documents_Management.Exceptions.*;
import com.API.Documents_Management.Repositories.RoleRepo;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Services.AuthorityService;
import com.API.Documents_Management.Services.RoleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private AppUserRepo appUserRepo;


    @Override
    public Role addRole(String roleName) {

        // Check if role name is valid
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleNameException("Invalid role name: " + roleName);
        }

        // check if role already exist
        if (roleRepo.findByName(roleType).isPresent()) {
            throw new RoleAlreadyExistsException("Role " + roleName + " already exists");
        }

        // create new role
        Role role = new Role();
        role.setName(roleType);
        role.setAuthorities(new HashSet<>());
        return roleRepo.save(role);
    }

    @Override
    public List<Role> addListOfRoles(List<String> roleNames) {

        List<Role> addedRoles = new ArrayList<>();

        for (String roleName : roleNames) {
            try {
                Role role = addRole(roleName);
                addedRoles.add(role);

            } catch (RoleAlreadyExistsException | InvalidRoleNameException e) {
                System.out.println("Skipping role '" + roleName + "': " + e.getMessage());
            }
        }

        return addedRoles;
    }

    @Override
    public Role getRoleByName(String roleName) {
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleNameException("Invalid role name: " + roleName);
        }

        return roleRepo.findByName(roleType)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleRepo.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role with id " + roleId + " not found"));
    }

    @Override
    @Transactional
    public Role deleteRoleByName(String name) {
        Role role = getRoleByName(name);

        // Delete role from user
        appUserRepo.findByRolesContaining(role).forEach(user -> {
            user.getRoles().remove(role);
            appUserRepo.save(user);
        });

        // Delete authoritiees
        role.getAuthorities().clear();
        roleRepo.save(role);

        roleRepo.delete(role);
        return role;
    }

    @Override
    @Transactional
    public Role deleteRoleById(Long id) {
        Role role = getRoleById(id);

        // use delete by role name
        return deleteRoleByName(role.getName().name());
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }


    @Override
    public void assignAuthorityToRole(String roleName, String authorityName) {
        RoleType roleType;

        try {
            // Check if role is valid

            roleType = RoleType.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleNameException("Invalid role name: " + roleName);
        }

        // check if role exist
        Role role = roleRepo.findByName(roleType)
                .orElseThrow(() -> new EntityNotFoundException("Role " + roleName+ " not found"));
        Authority authority = authorityService.getAuthorityByName(authorityName);

        // Check if authority already assigned to this role
        if (!role.getAuthorities().add(authority)) {
            throw new IllegalStateException("Authority "+authorityName+" is already assigned to role "+roleName);
        }
        roleRepo.save(role);
    }

    @Override
    public void deleteAuthorityFromRole(String roleName, String authorityName) {
        RoleType roleType;
        try {
            // check if role name is valid
            roleType = RoleType.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleNameException("Invalid role name: " + roleName);
        }

        // check if role exist
        Role role = roleRepo.findByName(roleType)
                .orElseThrow(() -> new EntityNotFoundException("Role " + roleName+ " not found"));

        Authority authority = authorityService.getAuthorityByName(authorityName);

        // Remove authority from role
        boolean removed = role.getAuthorities().remove(authority);

        if (!removed) {
            throw new IllegalStateException(
                    "Authority "+authorityName+" is not assigned to role"+roleName );
        }

        roleRepo.save(role);
    }

}