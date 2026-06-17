package com.employeehub.employeehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessSeconds,
        long refreshSeconds,
        String issuer,
        String accessCookieName,
        String refreshCookieName
) {}