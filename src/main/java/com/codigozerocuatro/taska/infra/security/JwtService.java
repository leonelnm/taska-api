package com.codigozerocuatro.taska.infra.security;

import com.codigozerocuatro.taska.domain.model.User;
import com.codigozerocuatro.taska.infra.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@Configuration
public class JwtService {

    private final AppProperties appProperties;
    private final SecretKey key;

    public JwtService(@NotNull AppProperties appProperties) {
        this.appProperties = appProperties;
        this.key = Keys.hmacShaKeyFor(appProperties.jwt().secret().getBytes());
    }

    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.jwt().expiration());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.jwt().refreshExpiration());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    public boolean isNotRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return !"refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return true;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            log.error("Error parsing claims from token: {}", ex.getMessage());
            return null;
        }
    }

}
