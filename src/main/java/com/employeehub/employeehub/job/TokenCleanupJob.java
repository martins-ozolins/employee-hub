package com.employeehub.employeehub.job;

import com.employeehub.employeehub.repository.RefreshTokenRepository;
import com.employeehub.employeehub.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class TokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupJob.class);

    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupJob(VerificationTokenRepository verificationTokenRepository, RefreshTokenRepository refreshTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 */8 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        verificationTokenRepository.deleteByExpiresAtBefore(now);
        refreshTokenRepository.deleteByExpiresAtBefore(now);
        log.info("Expired tokens cleaned up");
    }

}