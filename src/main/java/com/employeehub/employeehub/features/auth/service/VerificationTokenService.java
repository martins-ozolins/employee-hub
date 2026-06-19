package com.employeehub.employeehub.features.auth.service;

import com.employeehub.employeehub.features.auth.entity.TokenType;
import com.employeehub.employeehub.features.auth.entity.User;
import com.employeehub.employeehub.features.auth.entity.VerificationToken;
import com.employeehub.employeehub.features.auth.repository.VerificationTokenRepository;
import com.employeehub.employeehub.shared.exception.InvalidCredentialsException;
import com.employeehub.employeehub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Transactional
    public UUID generateToken(User user, TokenType type) {
        UUID tokenValue = UUID.randomUUID();

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .token(tokenValue)
                .type(type)
                .expiresAt(getExpiry(type))
                .build();

        verificationTokenRepository.save(token);
        return tokenValue;
    }

    @Transactional
    public User verifyToken(UUID tokenValue, TokenType type) {
        VerificationToken token = verificationTokenRepository
                .findByTokenAndType(tokenValue, type)
                .orElseThrow(() -> new NotFoundException("Token not found."));

        if (token.getUsedAt() != null) {
            throw new InvalidCredentialsException();
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException();
        }

        token.setUsedAt(Instant.now());
        return token.getUser();
    }

    private Instant getExpiry(TokenType type) {
        return switch (type) {
            case EMAIL_VERIFICATION -> Instant.now().plus(24, ChronoUnit.HOURS);
            case PASSWORD_RESET -> Instant.now().plus(2, ChronoUnit.HOURS);
        };
    }

}
