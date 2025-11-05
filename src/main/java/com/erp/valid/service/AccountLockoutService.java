package com.erp.valid.service;

import com.erp.valid.entity.User;
import com.erp.valid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockoutService {

    private final UserRepository userRepository;

    @Value("${security.lockout.max-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lockout.duration-minutes:30}")
    private int lockDurationMinutes;

    /**
     * Handle failed login attempt
     */
    @Transactional
    public void handleFailedLogin(User user) {
        //user.incrementFailedAttempts();
        user.setFailedLoginAttempts(user.getFailedLoginAttempts()+1);
        log.warn("Failed login attempt #{} for user: {}",
                user.getFailedLoginAttempts(), user.getEmail());


        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.lockAccount();

            log.warn("Account locked for user: {} after {} failed attempts",
                        user.getEmail(), maxFailedAttempts);
        }
        userRepository.saveAndFlush(user);
    }

    /**
     * Handle successful login
     */
    @Transactional
    public void handleSuccessfulLogin(User user) {
        if (user.getFailedLoginAttempts() > 0 || user.isAccountLocked()) {
            log.info("Resetting failed attempts for user: {}", user.getEmail());
            user.resetFailedAttempts();
            userRepository.save(user);
        }
    }

    /**
     * Check if account is locked and handle lock expiration
     */
    @Transactional
    public boolean isAccountLocked(User user) {
        if (!user.isAccountLocked()) {
            return false;
        }

        // Check if lock has expired
        if (user.isLockExpired(lockDurationMinutes)) {
            log.info("Lock expired for user: {}. Unlocking account.", user.getEmail());
            user.resetFailedAttempts();
            userRepository.save(user);
            return false;
        }

        return true;
    }

    /**
     * Get remaining lock time in minutes
     */
    public long getRemainingLockTime(User user) {
        if (!user.isAccountLocked() || user.getLockTime() == null) {
            return 0;
        }

        LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
        Duration duration = Duration.between(LocalDateTime.now(), unlockTime);

        return Math.max(0, duration.toMinutes());
    }

    /**
     * Get remaining failed attempts before lockout
     */
    public int getRemainingAttempts(User user) {
        return Math.max(0, maxFailedAttempts - user.getFailedLoginAttempts());
    }

    /**
     * Manually unlock account (admin function)
     */
    @Transactional
    public void unlockAccount(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            log.info("Manually unlocking account for user: {}", email);
            user.resetFailedAttempts();
            userRepository.save(user);
        });
    }
}