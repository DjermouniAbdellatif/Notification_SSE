package com.API.Documents_Management.Dto;

import com.API.Documents_Management.Entities.AppUser;

import java.util.List;

public class GetUserDTO {

    private String username;
    private List<String> roles;
    private List<String> authorities;
    private Long divisionId;
    private Long directionId;
    private Long sousDirectionId;

    public GetUserDTO() {
    }

    public GetUserDTO(String username, List<String> roles, List<String> authorities, Long divisionId, Long directionId, Long sousDirectionId) {
        this.username = username;
        this.roles = roles;
        this.authorities = authorities;
        this.divisionId = divisionId;
        this.directionId = directionId;
        this.sousDirectionId = sousDirectionId;
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

    public Long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Long divisionId) {
        this.divisionId = divisionId;
    }

    public Long getDirectionId() {
        return directionId;
    }

    public void setDirectionId(Long directionId) {
        this.directionId = directionId;
    }

    public Long getSousDirectionId() {
        return sousDirectionId;
    }

    public void setSousDirectionId(Long sousDirectionId) {
        this.sousDirectionId = sousDirectionId;
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

        Long divisionId = (user.getDivision() != null) ? user.getDivision().getId() : null;
        Long directionId = (user.getDirection() != null) ? user.getDirection().getId() : null;
        Long sousDirectionId = (user.getSousDirection() != null) ? user.getSousDirection().getId() : null;

        return new GetUserDTO(
                user.getUsername(),
                roleNames,
                authorityNames,
                divisionId,
                directionId,
                sousDirectionId
        );
    }

}