package com.erp.valid.service;

import com.erp.valid.dto.*;
import com.erp.valid.entity.Role;
import com.erp.valid.entity.User;
import com.erp.valid.exception.ConflictException;
import com.erp.valid.exception.RoleNotFoundException;
import com.erp.valid.repository.RoleRepository;
import com.erp.valid.repository.UserRepository;
import com.erp.valid.util.JwtUtil;
import com.erp.valid.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpCacheService otpCacheService;
    private final JwtUtil jwtUtil;
    private final AccountLockoutService lockoutService;


    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ConflictException("Passwords do not match");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Default role not found"));

        String otp = OtpUtil.generateOtp();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(defaultRole);
        user.setActive(false);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        otpCacheService.storeOtp(savedUser.getEmail(), otp);
        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                "Registration successful! Please check your email for OTP verification."
        );
    }

    @Transactional
    public AuthResponse verifyOtpAndGenerateToken(VerifyOtpRequest request) {
        log.info("Attempting to verify OTP for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ConflictException("User not found"));

        if (user.isActive()) {
            throw new ConflictException("Account already verified");
        }

        String storedOtp = otpCacheService.getOtp(request.getEmail());

        if (storedOtp == null) {
            log.warn("OTP expired or not found for email: {}", request.getEmail());
            throw new ConflictException("OTP has expired. Please request a new one.");
        }

        if (!request.getOtp().equals(storedOtp)) {
            log.warn("Invalid OTP attempt for email: {}", request.getEmail());
            throw new ConflictException("Invalid OTP");
        }

        user.setActive(true);
        userRepository.save(user);

        otpCacheService.deleteOtp(request.getEmail());

        log.info("User verified successfully: {}", user.getEmail());

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getName()
        );

        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        otpCacheService.logCacheStats();

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName(),
                "Email verified successfully! You are now logged in."
        );
    }

    @Transactional
    public UserResponse resendOtp(String email) {
        log.info("Attempting to resend OTP for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ConflictException("User not found"));

        if (user.isActive()) {
            throw new ConflictException("Account already verified");
        }

        String newOtp = OtpUtil.generateOtp();
        otpCacheService.storeOtp(user.getEmail(), newOtp);

        log.info("New OTP generated for email: {}", email);

        emailService.sendOtpEmail(user.getEmail(), newOtp);

        return new UserResponse(
                null,
                null,
                user.getEmail(),
                "New OTP sent to your email!"
        );
    }

    @Transactional()
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ConflictException("Invalid email or password"));
        // Check if account is locked
        if (lockoutService.isAccountLocked(user)) {
            long remainingMinutes = lockoutService.getRemainingLockTime(user);
            log.warn("Login attempt on locked account: {}", user.getEmail());
            throw new ConflictException(
                    String.format("Account is locked due to multiple failed login attempts. " +
                            "Please try again in %d minutes.", remainingMinutes)
            );
        }
        if (!user.isActive()) {
            throw new ConflictException("Account not verified. Please verify your email first.");
        }

        if (!user.isEnabled()) {
            throw new ConflictException("Account is disabled. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            lockoutService.handleFailedLogin(user);

            User updatedUser = userRepository.findByEmail(request.getEmail()).orElseThrow();
            int remainingAttempts = lockoutService.getRemainingAttempts(updatedUser);
            if (remainingAttempts > 0) {
                throw new ConflictException(
                        String.format("Invalid email or password. %d attempts remaining.",
                                remainingAttempts)
                );
            } else {
                throw new ConflictException(
                        "Invalid email or password. Account has been locked due to " +
                                "multiple failed attempts."
                );
            }
        }

// Successful login - reset failed attempts
        lockoutService.handleSuccessfulLogin(user);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getName()
        );

        log.info("User logged in successfully: {}", user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName(),
                "Login successful!"
        );
    }
}