package com.employeehub.employeehub.repository;

import com.employeehub.employeehub.entity.TokenType;
import com.employeehub.employeehub.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenAndType(UUID token, TokenType type);

    void deleteByExpiresAtBefore(Instant cutoff);
}