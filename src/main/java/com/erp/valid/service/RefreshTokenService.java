package com.erp.valid.service;

import com.erp.valid.entity.RefreshToken;
import com.erp.valid.entity.User;
import com.erp.valid.exception.ConflictException;
import com.erp.valid.repository.RefreshTokenRepository;
import com.erp.valid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    /**
     * Creates a new Refresh Token for the user, replacing any existing one.
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ConflictException("User not found for refresh token generation"));

        // Delete existing refresh token to enforce one token per user (security practice)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        log.info("New refresh token created for user: {}", user.getEmail());
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Finds and validates the Refresh Token.
     * Throws an exception if expired.
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            log.warn("Expired refresh token deleted: {}", token.getToken());
            throw new ConflictException("Refresh token was expired. Please make a new login request");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ConflictException("User not found"));
        refreshTokenRepository.deleteByUser(user);
        log.info("Refresh token deleted for user ID: {}", userId);
    }
}