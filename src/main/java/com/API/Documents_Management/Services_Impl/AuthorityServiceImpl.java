package com.API.Documents_Management.Services_Impl;

import com.API.Documents_Management.Entities.Authority;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Enums.AuthorityName;

import com.API.Documents_Management.Enums.RoleType;
import com.API.Documents_Management.Exceptions.InvalidAuthorityNameException;
import com.API.Documents_Management.Exceptions.AuthorityAlreadyExistsException;
import com.API.Documents_Management.Exceptions.InvalidRoleNameException;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Repositories.AuthorityRepo;
import com.API.Documents_Management.Repositories.RoleRepo;
import com.API.Documents_Management.Services.AuthorityService;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthorityServiceImpl implements AuthorityService {

    @Autowired
    private AuthorityRepo authorityRepo;


    @Autowired
    private RoleRepo roleRepo;




    @Override
    public Authority addAuthority(String authorityNameStr) {

        // Check if authority name exist
        AuthorityName authorityName;
        try {
            authorityName = AuthorityName.valueOf(authorityNameStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidAuthorityNameException("Invalid authority name: " + authorityNameStr);
        }

        // Check if authority already exist
        if (authorityRepo.findByName(authorityName).isPresent()) {
            throw new AuthorityAlreadyExistsException("Authority " + authorityNameStr + " already exists");
        }

        // Create authority
        Authority authority = new Authority();
        authority.setName(String.valueOf(authorityName));

        return authorityRepo.save(authority);
    }


    @Override
    public List<Authority> addListOfAuthorities(List<String> authorityNames) {
        List<Authority> addedAuthoritys = new ArrayList<>();
        Logger logger = LoggerFactory.getLogger(AuthorityServiceImpl.class);

        for (String authorityName : authorityNames) {
            try {
                Authority authority = addAuthority(authorityName);
                addedAuthoritys.add(authority);
                logger.info("Authority '{}' added successfully.", authorityName);

            } catch (AuthorityAlreadyExistsException e) {
                logger.warn("Skipping authority '{}': Authority already exists.", authorityName);
            } catch (InvalidAuthorityNameException e) {
                logger.warn("Skipping authority '{}': Invalid authority name.", authorityName);
            } catch (Exception e) {
                logger.error("Unexpected error occurred while adding authority '{}': {}", authorityName, e.getMessage());
            }
        }

        return addedAuthoritys;
    }

    @Override
    public Authority getAuthorityByName(String authorityNameStr) {
        AuthorityName authorityName;
        try {
            authorityName = AuthorityName.valueOf(authorityNameStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidAuthorityNameException("Invalid authority name: " + authorityNameStr);
        }

        return authorityRepo.findByName(authorityName)
                .orElseThrow(() -> new EntityNotFoundException("Authority not found: " + authorityNameStr));
    }

    @Override
    public Authority getAuthorityById(Long authorityId) {
        return authorityRepo.findById(authorityId)
                .orElseThrow(() -> new EntityNotFoundException("Authority with id " + authorityId + " not found"));
    }


    @Override
    @Transactional
    public Authority deleteAuthorityByName(String name) {

        // Get Authority or throw exception
        Authority authority = getAuthorityByName(name);

        // Delete relation Authority ←→ Role

        roleRepo.findByAuthoritiesContaining(authority).forEach(role -> {
            role.getAuthorities().remove(authority);
            roleRepo.save(role);
        });

        // Delete authority now
        authorityRepo.delete(authority);
        return authority;
    }

    @Override
    @Transactional
    public Authority deleteAuthorityById(Long id) {

        Authority authority = getAuthorityById(id);
        // use delete by authority name
        return deleteAuthorityByName(authority.getName().name());
    }

    @Override
    public List<Authority> getAllAuthorities() {
        return authorityRepo.findAll();
    }


}