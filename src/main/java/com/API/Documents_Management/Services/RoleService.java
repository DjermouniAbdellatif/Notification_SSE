package com.API.Documents_Management.Services;

import com.API.Documents_Management.Dto.ApiResponse;
import com.API.Documents_Management.Entities.Role;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface RoleService  {
    void assignAuthorityToRole(String roleName, String authorityName);
    void deleteAuthorityFromRole(String roleName, String authorityName);


    Role addRole(String roleName);

     List<Role> addListOfRoles(List<String> roleNames);

    Role getRoleByName(String roleNAme);
    Role getRoleById(Long roleId);

   Role deleteRoleByName(String name);
   Role deleteRoleById(Long roleId);

    List<Role> getAllRoles();
}

