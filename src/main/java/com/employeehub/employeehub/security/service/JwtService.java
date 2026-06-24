package com.employeehub.employeehub.security.service;

import com.employeehub.employeehub.config.JwtProperties;
import com.employeehub.employeehub.features.auth.dto.AuthDtos.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessSeconds;
    private final long refreshSeconds;
    private final String issuer;

    public JwtService(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = jwtProperties.accessSeconds();
        this.refreshSeconds = jwtProperties.refreshSeconds();
        this.issuer = jwtProperties.issuer();
    }

    /** Create a short-lived access token */
    public String generateAccessToken(String email, UUID id, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(email) // subject = user identity
                .claim("userId", id.toString())
                .claim("jti", UUID.randomUUID().toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key) // HS256 auto-chosen based on key type
                .compact();
    }

    /** Create a long-lived refresh token */
    public RefreshTokenResult generateRefreshToken(String email, UUID userId, String role) {
        UUID jti = UUID.randomUUID();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshSeconds);

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(email)
                .claim("userId", userId.toString())
                .claim("jti", jti.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();

        return new RefreshTokenResult(token, jti, exp);
    }

    public JwtClaims validateJwtAndGetClaims(String jwt) throws JwtException {
        Claims payload = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        String email = payload.getSubject();
        UUID userId = UUID.fromString(payload.get("userId", String.class));
        UUID jti = UUID.fromString(payload.get("jti", String.class));
        String role = payload.get("role", String.class);

        return new JwtClaims(email, userId, jti, role);
    }

}