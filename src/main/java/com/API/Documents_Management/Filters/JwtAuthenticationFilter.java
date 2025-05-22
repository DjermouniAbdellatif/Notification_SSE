package com.API.Documents_Management.Filters;


import com.API.Documents_Management.Entities.Token;
import com.API.Documents_Management.Repositories.AppUserRepo;
import com.API.Documents_Management.Repositories.TokenRepo;
import com.API.Documents_Management.Services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepo tokenRepo;
    private final AppUserRepo appUserRepo;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   TokenRepo tokenRepo,
                                   AppUserRepo appUserRepo) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepo = tokenRepo;
        this.appUserRepo = appUserRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }



        try {

            String token = authHeader.substring(7);

            String username = jwtService.extractUsernameFromToken(token);


            // check if token is valid

            if(!jwtService.isValidToken(token)){
                filterChain.doFilter(request, response);
                return;
            }

            // Forbiden access if Token is revoked or expîred

            boolean isRefreshToken = jwtService.isRefreshToken(token);

            // si refresh token

            if(isRefreshToken){

            // verifier sil existe dans BDD

                Token validToken= tokenRepo.findByToken(token).orElse(null);
                if(validToken != null){

            // verifier sil expired or revoked

                    if(validToken.getExpired()&& validToken.getRevoked()){

                        filterChain.doFilter(request, response);
                        return;
                    }

                }

            }else {
                // Check if user Refresh Token is Valid

                Long userId=appUserRepo.findAppUserByUsername(username).get().getId().longValue();

                List<Token> refreshToken = tokenRepo.findAllValidTokenByUser(userId);

                if(refreshToken.isEmpty()){

                    filterChain.doFilter(request, response);
                    return;
                }
            }



            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                List<String> permissions = jwtService.extractPermissions(token);



                // Convertir en GrantedAuthority
                List<GrantedAuthority> authorities = permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (JwtException e) {

            logger.error("Failed to process JWT: {}");
        }

        filterChain.doFilter(request, response);
    }
}