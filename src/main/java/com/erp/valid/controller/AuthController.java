package com.erp.valid.controller;

import com.erp.valid.dto.RegisterRequest;
import com.erp.valid.dto.UserResponse;
import com.erp.valid.dto.VerifyOtpRequest;
import com.erp.valid.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) throws Exception {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<UserResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        UserResponse response = userService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<UserResponse> resendOtp(@RequestParam String email) {
        UserResponse response = userService.resendOtp(email);
        return ResponseEntity.ok(response);
    }
}