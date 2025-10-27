package com.erp.valid.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpCacheService {

    private final Cache<String, String> otpCache;

    /**
     * Store OTP for an email with automatic 5-minute expiration
     */
    public void storeOtp(String email, String otp) {
        String key = generateKey(email);
        otpCache.put(key, otp);
        log.info("OTP stored in cache for email: {}", email);
    }

    /**
     * Retrieve OTP for an email
     * Returns null if OTP doesn't exist or has expired
     */
    public String getOtp(String email) {
        String key = generateKey(email);
        String otp = otpCache.getIfPresent(key);

        if (otp != null) {
            log.info("Cache HIT for email: {}", email);
        } else {
            log.info("Cache MISS for email: {} (expired or not found)", email);
        }

        return otp;
    }

    /**
     * Delete OTP from cache (used after successful verification)
     */
    public void deleteOtp(String email) {
        String key = generateKey(email);
        otpCache.invalidate(key);
        log.info("OTP deleted from cache for email: {}", email);
    }

    /**
     * Check if OTP exists in cache
     */
    public boolean hasOtp(String email) {
        String key = generateKey(email);
        return otpCache.getIfPresent(key) != null;
    }

    /**
     * Log cache statistics for monitoring
     */
    public void logCacheStats() {
        var stats = otpCache.stats();
        log.info("Cache Statistics - Hits: {}, Misses: {}, Hit Rate: {:.2f}%, Evictions: {}",
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate() * 100,
                stats.evictionCount());
    }

    /**
     * Generate consistent cache key for email
     */
    private String generateKey(String email) {
        return "otp:" + email.toLowerCase();
    }
}