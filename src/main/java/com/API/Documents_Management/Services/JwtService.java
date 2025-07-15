package com.API.Documents_Management.Services;


import com.API.Documents_Management.Dto.TokenPair;
import com.API.Documents_Management.Entities.Role;
import com.API.Documents_Management.Enums.RoleType;
import com.API.Documents_Management.Repositories.RoleRepo;
import com.API.Documents_Management.Services_Impl.CustomUserDetails;
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

    // 🔑 Générer access token
    public String generateAccessToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "access");
        return generateToken(authentication, jwtExpirationMs, claims);
    }

    // 🔑 Générer refresh token
    public String generateRefreshToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return generateToken(authentication, refreshExpirationMs, claims);
    }

    // 🔑 Générer les deux tokens
    public TokenPair generateTokenPair(Authentication authentication) {
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);
        return new TokenPair(accessToken, refreshToken);
    }

    // ✅ Générer token avec claims
    private String generateToken(Authentication authentication, Long expirationMs, Map<String, Object> claims) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        String username = userPrincipal.getUsername();
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        // Rôles depuis AppUser
        List<String> roles = userPrincipal.getUser().getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        claims.put("roles", roles);

        // Authorities depuis AppUser
        Set<String> authorities = new HashSet<>();
        userPrincipal.getUser().getRoles().forEach(role -> {
            role.getAuthorities().forEach(authority -> {
                authorities.add(authority.getName().name());
            });
        });
        claims.put("authorities", authorities);

        return Jwts.builder()
                .header()
                .and()
                .subject(username)
                .claims(claims)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigninKey())
                .compact();
    }

    // ✅ Validation token pour un user
    public Boolean validateTokenForUser(String token, UserDetails userDetails) {
        final String username = extractUsernameFromToken(token);
        return (username != null && username.equals(userDetails.getUsername()));
    }

    public Boolean isValidToken(String token) {
        return extractUsernameFromToken(token) != null;
    }

    public String extractUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    public Boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null && "refresh".equals(claims.get("tokenType"));
    }

    // ✅ Extraire tous les claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    // ✅ Extraire rôles
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

    // ✅ Extraire permissions (authorities)
    public List<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        Object authObj = claims.get("authorities");
        if (authObj instanceof List<?>) {
            return ((List<?>) authObj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // ✅ Générer clé de signature
    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
