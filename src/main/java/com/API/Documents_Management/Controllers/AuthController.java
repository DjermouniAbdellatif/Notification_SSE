package com.API.Documents_Management.Controllers;

import com.API.Documents_Management.Dto.LoginRequest;
import com.API.Documents_Management.Dto.RefreshTokenRequest;
import com.API.Documents_Management.Dto.RegisterRequest;
import com.API.Documents_Management.Dto.TokenPair;
import com.API.Documents_Management.Services.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {

        //save the new user to database
        authService.registerUser(request);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        System.out.println("✅ Login endpoint called");

        TokenPair tokenPair = authService.login(request);


        return ResponseEntity.ok(tokenPair);

    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {

        TokenPair tokenPair=authService.refreshToken(request);
        return ResponseEntity.ok(tokenPair);
    }
}
