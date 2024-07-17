package com.iapex.service.security;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Hex;
import com.iapex.model.user.UserMobile;
import com.iapex.model.user.UserWeb;
import com.iapex.repository.token.TokenWebRepository;
import com.iapex.repository.token.TokenMobileRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtService {
	
    @Autowired
    private TokenWebRepository tokenWebRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final String SECRET_KEY = "d37662fcfcdde9c0d8515abd9f3054d82feecb7d9ead975c0b3b10ef79c7f6eb";
    private final TokenMobileRepository tokenMobileRepository;

    public JwtService(TokenMobileRepository tokenMobileRepository) {
        this.tokenMobileRepository = tokenMobileRepository;
    }

    public String extractEmail(String token) {
        String email = extractClaim(token, Claims::getSubject);
        //logger.debug("Extracted email from token: {}", email);
        return email;
    }

    public boolean isValid(String token, UserDetails user) {
        String email = extractEmail(token);
        //logger.debug("Validating token for email: {}", email);
        //logger.debug("For user: {}", email);
        boolean validToken;
        if (user instanceof UserMobile) {
            validToken = tokenMobileRepository.findByToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);
        } else if (user instanceof UserWeb) {
            validToken = tokenWebRepository.findByToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);
        } else {
            validToken = false;
        }

        boolean isValid = (email.equals(user.getUsername())) && !isTokenExpired(token) && validToken;
        //logger.debug("Token validation result: isValid={}, emailMatch={}, notExpired={}, validInRepo={}",isValid, email.equals(user.getUsername()), !isTokenExpired(token), validToken);
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean isExpired = expiration.before(new Date());
        //logger.debug("Token expiration check. Expiration: {}, Is expired: {}", expiration, isExpired);
        return isExpired;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        logger.debug("Extracting all claims from token");
        return Jwts
            .parser()
            .verifyWith(getSigninKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = claims.get("authorities", List.class);
        logger.debug("Extracted authorities from token: {}", roles);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public String generateToken(UserMobile user) {
        //logger.debug("Generating token for user: {}", user.getEmail());
        String token = Jwts
            .builder()
            .subject(user.getEmail())
            .claim("authorities", user.getRole().getAuthorities())
            .issuedAt(new Date(System.currentTimeMillis()))
            //.expiration(new Date(System.currentTimeMillis() + 60*1000 )) //1 MINUTO
            .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) //10 MINUTOS
            .signWith(getSigninKey())
            .compact();
        //logger.debug("Generated token: {}", token);
        return token;
    }

    public String generateTokenUserWeb(UserWeb userWeb) {
        //logger.debug("Generating token for user web: {}", userWeb.getEmail());
        String token = Jwts
            .builder()
            .subject(userWeb.getEmail())
            .claim("authorities", userWeb.getRole().getAuthorities())
            .issuedAt(new Date(System.currentTimeMillis()))
            //.expiration(new Date(System.currentTimeMillis() + 60*1000 )) //1 MINUTO
            .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) //10 MINUTOS
            .signWith(getSigninKey())
            .compact();
        //logger.debug("Generated token: {}", token);
        return token;
    }

    private SecretKey getSigninKey() {
        byte[] keyBytes = Hex.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserMobile user) {
        String email = extractEmail(token);
        boolean isValid = (email.equals(user.getEmail())) && !isTokenExpired(token);
        //logger.debug("Token validation for user. Email: {}, Is valid: {}", email, isValid);
        return isValid;
    }
}