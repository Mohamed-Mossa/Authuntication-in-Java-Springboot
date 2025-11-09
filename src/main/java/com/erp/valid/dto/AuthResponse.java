package com.erp.valid.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String message;

    public AuthResponse(String token, Long userId, String username, String email, String role, String message,String refreshToken) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.message = message;
        this.refreshToken = refreshToken;
    }

    public AuthResponse(String token, String refreshToken, Long id, String username, String email, String name, String refreshToken1) {
    }
}