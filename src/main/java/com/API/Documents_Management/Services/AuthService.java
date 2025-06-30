package com.API.Documents_Management.Services;

import com.API.Documents_Management.Dto.LoginRequest;
import com.API.Documents_Management.Dto.RefreshTokenRequest;
import com.API.Documents_Management.Dto.RegisterRequest;
import com.API.Documents_Management.Dto.TokenPair;
import com.API.Documents_Management.Entities.AppUser;

public interface AuthService {

    void registerUser(RegisterRequest registerRequest);

    TokenPair login(LoginRequest loginRequest);

    TokenPair refreshToken(RefreshTokenRequest request);

    AppUser getAuthenticatedUser();
}
