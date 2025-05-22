package com.API.Documents_Management.Services;

import com.API.Documents_Management.Entities.Authority;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AuthorityService {

    Authority addAuthority(String roleName);

    List<Authority> addListOfAuthorities(List<String> roleNames);


    Authority getAuthorityByName(String roleNAme);
    Authority getAuthorityById(Long roleId);

    Authority deleteAuthorityByName(String name);
    Authority deleteAuthorityById(Long roleId);

    List<Authority> getAllAuthorities();



}

