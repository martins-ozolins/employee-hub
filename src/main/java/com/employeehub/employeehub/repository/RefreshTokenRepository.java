package com.employeehub.employeehub.repository;

import com.employeehub.employeehub.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJti(UUID jti);

    void deleteByExpiresAtBefore(Instant cutoff);
}
