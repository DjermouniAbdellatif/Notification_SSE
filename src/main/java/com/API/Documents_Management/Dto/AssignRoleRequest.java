package com.API.Documents_Management.Dto;

public class AssignRoleRequest {
   private   String username;
   private String roleName;

   public AssignRoleRequest(String username, String roleName) {
       this.username = username;
       this.roleName = roleName;
   }
   public AssignRoleRequest() {};

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
