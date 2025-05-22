package com.API.Documents_Management.Dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data

public class LoginRequest {

    @NotBlank(message = "Username is Required")
    private String username;

    @NotBlank(message = "Password is Required")
    private String password;


    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
