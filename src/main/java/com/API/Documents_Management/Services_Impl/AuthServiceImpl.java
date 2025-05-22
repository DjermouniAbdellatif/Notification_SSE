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


import com.API.Documents_Management.Services.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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

    // Register User Methode

    @Transactional
    public void registerUser(RegisterRequest registerRequest) {

        if(userRepo.findAppUsersByUsernameWithRoles(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already Existe");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(registerRequest.getUsername());
        appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        Set<Role> persistentRoles = new HashSet<>();
        registerRequest.getRoles().forEach(role -> {
            Role persistentRole = roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role " + role.getName() + " not found"));
            persistentRoles.add(persistentRole);
        });
        appUser.setRoles(persistentRoles);


        userRepo.save(appUser);
    }


    // Login Method

    public TokenPair login(LoginRequest loginRequest){

        //Authenticate User

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        TokenPair tokenPair=jwtService.generateTokenPair(authentication);

        AppUser appUser = userRepo.findAppUserByUsername(loginRequest.getUsername()).orElse(null);

        if(appUser==null) {
            throw new UserNotFoundException(loginRequest.getUsername());
        }

         // revoke all old tokens
        revokeAllUserTokens(appUser);


        saveUserToken(appUser,tokenPair.getRefreshToken());

        // Set security on security Context

        SecurityContextHolder.getContext().setAuthentication(authentication);



        return tokenPair;

    }



    // Generate new Tokens Method

    public TokenPair refreshToken(RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        // Vérifier si le refresh token est valide
        
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or has expired.");
        }



        String username = jwtService.extractUsernameFromToken(refreshToken);



        UserDetails userDetails = userDetailsService.loadUserByUsername(username);



        if (userDetails == null) {
            throw new UserNotFoundException("User not found for username: " + username);
        }


        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Générer le nouveau access token

        String accessToken = jwtService.generateAccessToken(authenticationToken);



        return new TokenPair(accessToken, refreshToken);
    }


    // save token Methode
    private void saveUserToken(AppUser appUser, String jwtToken  ) {

        Token token= new Token();

        token.setToken(jwtToken);
        token.setRevoked(false);
        token.setExpired(false);
        token.setUser(appUser);
        token.setTokenType(String.valueOf(TokenType.REFRESH));

        tokenRepo.save(token);

    }

    // Methode to  Revoke all user Tokens
    private void revokeAllUserTokens(AppUser appUser){

        var validTokens=tokenRepo.findAllValidTokenByUser(appUser.getId());

        if(validTokens.isEmpty()){
            return;
        }

        validTokens.forEach(token -> {
           token.setRevoked(true);
           token.setExpired(true);

        });

        tokenRepo.saveAll(validTokens);
    }
}