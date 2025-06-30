package com.API.Documents_Management.Services_Impl;

import com.API.Documents_Management.Dto.LoginRequest;
import com.API.Documents_Management.Dto.RefreshTokenRequest;
import com.API.Documents_Management.Dto.RegisterRequest;
import com.API.Documents_Management.Dto.TokenPair;
import com.API.Documents_Management.Entities.AppUser;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Entities.Token;
import com.API.Documents_Management.Enums.TokenType;
import com.API.Documents_Management.Exceptions.InvalidRefreshTokenException;
import com.API.Documents_Management.Exceptions.UserNotFoundException;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Repositories.RoleRepo;
import com.API.Documents_Management.Repositories.TokenRepo;
import com.API.Documents_Management.Services.AuthService;


import com.API.Documents_Management.Services.CustomeUserService;
import com.API.Documents_Management.Services.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepo roleRepository;
    private final TokenRepo tokenRepo;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new user
     */
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        if (userRepo.findAppUsersByUsernameWithRoles(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(registerRequest.getUsername());
        appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Assign roles
        Set<Role> persistentRoles = new HashSet<>();
        registerRequest.getRoles().forEach(role -> {
            Role persistentRole = roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role " + role.getName() + " not found"));
            persistentRoles.add(persistentRole);
        });
        appUser.setRoles(persistentRoles);

        userRepo.save(appUser);
    }

    /**
     * User login
     */
    public TokenPair login(LoginRequest loginRequest) {
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Generate JWT token pair
        TokenPair tokenPair = jwtService.generateTokenPair(authentication);

        // Load user
        AppUser appUser = userRepo.findAppUserByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException(loginRequest.getUsername()));

        // Revoke old tokens
        revokeAllUserTokens(appUser);

        // Save new refresh token
        saveUserToken(appUser, tokenPair.getRefreshToken());

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("Authenticated user: {}", getAuthenticatedUser().getUsername());

        return tokenPair;
    }

    /**
     * Refresh access token using a valid refresh token
     */
    public TokenPair refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }

        String username = jwtService.extractUsernameFromToken(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
            throw new UserNotFoundException("User not found for username: " + username);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtService.generateAccessToken(authenticationToken);

        return new TokenPair(newAccessToken, refreshToken);
    }

    /**
     * Save a refresh token to database
     */
    private void saveUserToken(AppUser appUser, String jwtToken) {
        Token token = new Token();
        token.setToken(jwtToken);
        token.setRevoked(false);
        token.setExpired(false);
        token.setUser(appUser);
        token.setTokenType(String.valueOf(TokenType.REFRESH));

        tokenRepo.save(token);
    }

    /**
     * Revoke all valid tokens for a user
     */
    private void revokeAllUserTokens(AppUser appUser) {
        var validTokens = tokenRepo.findAllValidTokenByUser(appUser.getId());

        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });

        tokenRepo.saveAll(validTokens);
    }

    /**
     * Get the currently authenticated user
     */
    public AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userRepo.findAppUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
