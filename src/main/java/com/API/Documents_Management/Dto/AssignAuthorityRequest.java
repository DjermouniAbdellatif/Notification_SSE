package com.API.Documents_Management.Dto;

import jakarta.validation.constraints.NotBlank;

public class AssignAuthorityRequest {
    @NotBlank(message = "roleName is required")
    private String roleName;

    @NotBlank(message = "authorityName is required")
    private String authorityName;

    public AssignAuthorityRequest() {}

    public AssignAuthorityRequest(String roleName, String authorityName) {
        this.roleName = roleName;
        this.authorityName = authorityName;
    }

    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public String getAuthorityName() {
        return authorityName;
    }
    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }
}