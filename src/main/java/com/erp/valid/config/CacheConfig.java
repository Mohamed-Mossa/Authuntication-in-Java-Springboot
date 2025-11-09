package com.erp.valid.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

//    @Bean
//    public Cache<String, String> otpCache() {
//        return Caffeine.newBuilder()
//                .expireAfterWrite(5, TimeUnit.MINUTES)  // OTP expires after 5 minutes
//                .maximumSize(10_000)  // Maximum 10,000 OTPs in cache
//                .recordStats()  // Enable statistics tracking
//                .build();
//    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Ensures keys and values are stored as human-readable strings in Redis
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}