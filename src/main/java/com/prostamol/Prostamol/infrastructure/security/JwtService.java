package com.prostamol.Prostamol.infrastructure.security;

import com.prostamol.Prostamol.domain.model.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
        @Value("${jwt.secret}") @NonNull String secret,
        @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(@NonNull UUID userId, String email, @NonNull Role role) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(expirationMs)))
            .signWith(key)
            .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    public Role extractRole(String token) {
        String role = extractClaims(token).get("role", String.class);
        return role != null ? Role.valueOf(role) : Role.USER;
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
