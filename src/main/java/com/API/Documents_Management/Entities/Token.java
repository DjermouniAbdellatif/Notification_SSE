package com.API.Documents_Management.Entities;

import com.API.Documents_Management.Enums.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,length = 1000)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private Boolean expired;

    private Boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = TokenType.valueOf(tokenType);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }
}
