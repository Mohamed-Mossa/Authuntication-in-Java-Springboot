package com.erp.valid.controller;

import com.erp.valid.dto.RegisterRequest;
import com.erp.valid.dto.UserResponse;
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
    public ResponseEntity<UserResponse> register(@RequestBody  @Valid  RegisterRequest request) throws Exception {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }
}