package com.erp.valid.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpCacheService {

    //private final Cache<String, String> otpCache;
// Inject the RedisTemplate configured in CacheConfig
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${otp.expiration.minutes:1}")
    private int otpExpirationMinutes;

    /**
     * Store OTP for an email with automatic 5-minute expiration
     */
    public void storeOtp(String email, String otp) {
        String key = generateKey(email);
//        otpCache.put(key, otp);
        redisTemplate.opsForValue().set(
                key,
                otp,
                otpExpirationMinutes,
                TimeUnit.MINUTES);
        log.info("OTP stored in Redis cache for email: {} with expiration: {} minutes", email, otpExpirationMinutes);    }

    /**
     * Retrieve OTP for an email
     * Returns null if OTP doesn't exist or has expired
     */
    public String getOtp(String email) {
        String key = generateKey(email);
//        String otp = otpCache.getIfPresent(key);
        String otp = redisTemplate.opsForValue().get(key);

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
//        otpCache.invalidate(key);
        redisTemplate.delete(key);
        log.info("OTP deleted from Redis cache for email: {}", email);    }

    /**
     * Check if OTP exists in cache
     */
    public boolean hasOtp(String email) {
        String key = generateKey(email);
//        return otpCache.getIfPresent(key) != null;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * Log cache statistics for monitoring
     */
//    public void logCacheStats() {
//        var stats = otpCache.stats();
//        log.info("Cache Statistics - Hits: {}, Misses: {}, Hit Rate: {:.2f}%, Evictions: {}",
//                stats.hitCount(),
//                stats.missCount(),
//                stats.hitRate() * 100,
//                stats.evictionCount());
//    }

    /**
     * Generate consistent cache key for email
     */
    private String generateKey(String email) {
        return "otp:" + email.toLowerCase();
    }
}