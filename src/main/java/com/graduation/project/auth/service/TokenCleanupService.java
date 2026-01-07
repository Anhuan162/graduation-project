package com.graduation.project.auth.service;

import com.graduation.project.auth.repository.InvalidatedTokenRepository;
import jakarta.transaction.Transactional;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * Run daily at midnight to clean up expired invalidated tokens.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting token cleanup job");
        Date now = new Date();
        int deletedCount = invalidatedTokenRepository.deleteByExpiryTimeBefore(now);
        log.info("Deleted {} expired invalidated tokens", deletedCount);
    }
}
