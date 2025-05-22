package com.API.Documents_Management.Services;


import com.API.Documents_Management.Dto.TokenPair;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Enums.RoleType;
import com.API.Documents_Management.Repositories.RoleRepo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {


    @Autowired
    private RoleRepo roleRepo;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private Long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshExpirationMs;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);



    private static final String TOKEN_PREFIX = "Bearer ";

    // Generate access Token
    public String generateAccessToken(Authentication authentication) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "access");


        return generateToken(authentication,jwtExpirationMs,claims);


    }



    // Generate refresh Token
    public String generateRefreshToken(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date(); // time of creation
        Date expirationDate = new Date(now.getTime() + refreshExpirationMs); // Time of token Expiration

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");


        return generateToken(authentication,refreshExpirationMs,claims);


    }

    // Validate Token

    public Boolean validateTokenForUser(String token, UserDetails userDetails) {
        final String username = extractUsernameFromToken(token);

        return (username!=null && username.equals(userDetails.getUsername()) );
    }


    public Boolean isValidToken(String token) {
        return extractUsernameFromToken(token)!=null;
    }


    public String extractUsernameFromToken(String token) {

        Claims claims=extractAllClaims(token);

        if(claims!=null) {
            return claims.getSubject();
        }

        return null;
    }


    // Validate if Token is refresh Token

    public Boolean isRefreshToken(String token) {
       Claims claims= extractAllClaims(token);

       if(claims ==null) {
           return false;

       }

       return "refresh".equals(claims.get("tokenType"));

    }


    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    private String generateToken(Authentication authentication, Long expirationMs, Map<String, Object> claims) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        String username = userPrincipal.getUsername();

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);


        //Ajouter les roles au token

        List<String> roleNames = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        claims.put("roles",roleNames);


        // Ajouter toutes les authorities  au token

        Set<String> allAuthorities = new HashSet<>();

        for (String roleName : roleNames) {


            RoleType roleType = RoleType.valueOf(roleName.substring(5));  // Eliminer ("ROLE_")

            roleRepo.findByName(roleType).ifPresent(role -> {
                role.getAuthorities().forEach(authority -> {
                    allAuthorities.add(String.valueOf(authority.getName()));
                });
            });
        }

        claims.put("authorities", allAuthorities);






        return Jwts.builder()
                .header()
                .and()
                .subject(userPrincipal.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigninKey())
                .compact();
    }


    public TokenPair generateTokenPair(Authentication authentication) {

        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);

        return new TokenPair(accessToken, refreshToken);
    }

    // Extrait tous les claims du token JWT

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    // Extrait la liste des rôles depuis le token

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    //Extrait la liste des permission

    public List<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("authorities", List.class);
    }
}
