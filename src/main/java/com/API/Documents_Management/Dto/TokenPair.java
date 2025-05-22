package com.API.Documents_Management.Dto;


import lombok.Data;

@Data

public class TokenPair {

    private String accessToken;
    private String refreshToken;


    public TokenPair(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
