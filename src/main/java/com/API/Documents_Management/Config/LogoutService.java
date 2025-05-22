package com.API.Documents_Management.Config;

import com.API.Documents_Management.Repositories.TokenRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service

public class LogoutService implements LogoutHandler {

    @Autowired
    private TokenRepo tokenRepo;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {



      String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
            String jwt = authHeader.substring(7);

            var storedToken=tokenRepo.findByToken(jwt)
                    .orElse(null);

            if (storedToken != null) {
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                tokenRepo.save(storedToken);

            }

    }
}
