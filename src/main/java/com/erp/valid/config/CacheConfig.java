package com.erp.valid.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, String> otpCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)  // OTP expires after 5 minutes
                .maximumSize(10_000)  // Maximum 10,000 OTPs in cache
                .recordStats()  // Enable statistics tracking
                .build();
    }
}