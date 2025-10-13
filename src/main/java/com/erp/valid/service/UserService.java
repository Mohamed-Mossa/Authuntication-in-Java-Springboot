package com.erp.valid.service;

import com.erp.valid.dto.RegisterRequest;
import com.erp.valid.dto.UserResponse;
import com.erp.valid.entity.Role;
import com.erp.valid.entity.User;
import com.erp.valid.exception.ConflictException;
import com.erp.valid.exception.RoleNotFoundException;
import com.erp.valid.repository.RoleRepository;
import com.erp.valid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerUser(RegisterRequest request) throws Exception {
        UserResponse response = new UserResponse();

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
        Role defaultRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RoleNotFoundException("Default role not found"));



        // Create user
        User user = new User();
        LocalDateTime createdAt = LocalDateTime.now();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(defaultRole);
        user.setActive(false);
        user.setEnabled(true);
        user.setCreatedAt(createdAt);


        // Save user
        User savedUser = userRepository.save(user);

        // Return response
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setMessage("User registered successfully");

        return response;
    }
}