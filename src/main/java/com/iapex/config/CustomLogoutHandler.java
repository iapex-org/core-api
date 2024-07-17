package com.iapex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.iapex.models.token.TokenMobile;
import com.iapex.repositories.token.TokenMobileRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenMobileRepository tokenMovilRepository;

    public CustomLogoutHandler(TokenMobileRepository tokenMovilRepository) {
        this.tokenMovilRepository = tokenMovilRepository;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7);
        TokenMobile storedToken = tokenMovilRepository.findByToken(token).orElse(null);

        if(storedToken != null) {
            storedToken.setLoggedOut(true);
            tokenMovilRepository.save(storedToken);
        }
    }
}