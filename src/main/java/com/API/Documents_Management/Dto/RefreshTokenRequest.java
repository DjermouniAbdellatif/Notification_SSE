package com.API.Documents_Management.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RefreshTokenRequest {
    private String refreshToken;


    public String getRefreshToken() {
        return refreshToken;
    }
}
