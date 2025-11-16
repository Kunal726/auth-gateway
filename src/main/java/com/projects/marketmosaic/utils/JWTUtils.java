package com.projects.marketmosaic.utils;

import com.projects.marketmosaic.common.config.ZooKeeperConfig;
import com.projects.marketmosaic.constants.ErrorMessages;
import com.projects.marketmosaic.entity.UserEntity;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor

public class JWTUtils {
    private final String secret;

    private final Long expiration;

    private SecretKey key;

    @Autowired
    public JWTUtils(final ZooKeeperConfig zooKeeperConfig) {
        this.secret = zooKeeperConfig.getStringValueByKey("JWT_SECRET");
        this.expiration = Long.parseLong(zooKeeperConfig.getStringValueByKey("JWT_EXPIRATION"));
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails, UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());

        // Convert comma-separated roles to a list of authorities
        claims.put("role", user.getRole());

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Collection<SimpleGrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        String role = claims.get("role", String.class);
        return List.of(new SimpleGrantedAuthority(role));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw AuthException.invalidToken(ErrorMessages.JWT_PARSE_ERROR);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw AuthException.invalidToken(ErrorMessages.INVALID_TOKEN_FORMAT);
        }
        return authHeader.substring(7);
    }

    public String validateAndExtractUsername(String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        return extractUsername(token);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractName(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    public String generatePasswordResetToken(String userId) {
        return Jwts.builder()
                .subject(userId)  // Add the user ID as the subject
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)  // Sign with a secure key
                .compact();
    }
}
