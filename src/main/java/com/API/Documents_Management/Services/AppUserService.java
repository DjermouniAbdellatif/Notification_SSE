package com.API.Documents_Management.Services;

import com.API.Documents_Management.Dto.GetUserDTO;
import com.API.Documents_Management.Dto.UpdateUserRequest;
import com.API.Documents_Management.Dto.UserRequest;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Entities.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface AppUserService {
    GetUserDTO assignRoleToUser(String username, String roleName);
    GetUserDTO deleteRoleFromUser(String username, String roleName);
    GetUserDTO addAppUser(AppUser appUser);
    List<GetUserDTO> findAllAppUsers();
    GetUserDTO findByUsername(String username);
    GetUserDTO findAppUserById(Long id);
    GetUserDTO deleteAppUserById(Long id);
    GetUserDTO deleteAppUserByUsername(String username);
    GetUserDTO updateAppUser(Long id, UpdateUserRequest userUpdate);
    List<GetUserDTO> getUsersByRole(String roleName);
}
