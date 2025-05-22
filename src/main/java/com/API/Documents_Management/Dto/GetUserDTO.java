package com.API.Documents_Management.Dto;

import com.API.Documents_Management.Entities.AppUser;

import java.util.List;

public class GetUserDTO {

    private String username;
    private List<String> roles;
    private List<String> authorities;

    public GetUserDTO() {
    }

    public GetUserDTO(String username, List<String> roles, List<String> authorities) {
        this.username = username;
        this.roles = roles;
        this.authorities = authorities;
    }

    // getters & setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }


    // Map user to userDTO

    public static GetUserDTO toDto(AppUser user) {
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();

        List<String> authorityNames = user.getRoles().stream()
                .flatMap(r -> r.getAuthorities().stream())
                .map(a -> a.getName().name())
                .distinct()
                .toList();

        return new GetUserDTO(user.getUsername(), roleNames, authorityNames);
    }

}