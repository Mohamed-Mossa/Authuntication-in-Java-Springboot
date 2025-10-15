package com.erp.valid.service;

import com.erp.valid.dto.RegisterRequest;
import com.erp.valid.dto.UserResponse;
import com.erp.valid.dto.VerifyOtpRequest;
import com.erp.valid.entity.Role;
import com.erp.valid.entity.User;
import com.erp.valid.exception.ConflictException;
import com.erp.valid.exception.RoleNotFoundException;
import com.erp.valid.repository.RoleRepository;
import com.erp.valid.repository.UserRepository;
import com.erp.valid.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${otp.expiration.minutes:5}")
    private long otpExpirationMinutes;

    public UserResponse registerUser(RegisterRequest request) throws Exception {

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ConflictException("Passwords do not match");
        }

        // Check if username exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        // Check if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        // Get default role
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Default role not found"));

        // Generate OTP
        String otp = OtpUtil.generateOtp();

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(defaultRole);
        user.setActive(false);  // Not active until OTP verified
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(user);

        // Send OTP email
        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        // Return response
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setMessage("Registration successful! Please check your email for OTP verification.");

        return response;
    }

    public UserResponse verifyOtp(VerifyOtpRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ConflictException("User not found"));

        // Check if already verified
        if (user.isActive()) {
            throw new ConflictException("Account already verified");
        }

        // Check if OTP matches
        if (!request.getOtp().equals(user.getOtp())) {
            throw new ConflictException("Invalid OTP");
        }

        // Check if OTP expired
        LocalDateTime otpGeneratedTime = user.getOtpGeneratedTime();
        if (otpGeneratedTime.plusMinutes(otpExpirationMinutes).isBefore(LocalDateTime.now())) {
            throw new ConflictException("OTP has expired. Please request a new one.");
        }

        // Activate user
        user.setActive(true);
        user.setOtp(null);  // Clear OTP after verification
        user.setOtpGeneratedTime(null);
        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        // Return response
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMessage("Email verified successfully! You can now login.");

        return response;
    }

    public UserResponse resendOtp(String email) {

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ConflictException("User not found"));

        // Check if already verified
        if (user.isActive()) {
            throw new ConflictException("Account already verified");
        }

        // Generate new OTP
        String newOtp = OtpUtil.generateOtp();
        user.setOtp(newOtp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);

        // Send new OTP email
        emailService.sendOtpEmail(user.getEmail(), newOtp);

        // Return response
        UserResponse response = new UserResponse();
        response.setEmail(user.getEmail());
        response.setMessage("New OTP sent to your email!");

        return response;
    }
}