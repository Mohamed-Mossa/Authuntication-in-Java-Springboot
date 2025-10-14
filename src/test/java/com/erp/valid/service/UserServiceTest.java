package com.erp.valid.service;

import com.erp.valid.dto.RegisterRequest;
import com.erp.valid.dto.UserResponse;
import com.erp.valid.entity.Role;
import com.erp.valid.entity.User;
import com.erp.valid.exception.ConflictException;
import com.erp.valid.exception.RoleNotFoundException;
import com.erp.valid.repository.RoleRepository;
import com.erp.valid.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;
    private Role defaultRole;
    private User savedUser;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new RegisterRequest();
        validRequest.setUsername("testuser");
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("password123");
        validRequest.setConfirmPassword("password123");

        // Setup default role
        defaultRole = new Role();
        defaultRole.setId(1L);
        defaultRole.setName("ROLE_USER");

        // Setup saved user
        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(defaultRole);
        savedUser.setActive(false);
        savedUser.setEnabled(true);
        savedUser.setCreatedAt(LocalDateTime.now());
    }


    @Test
    @DisplayName("Should successfully register user with valid data")
    void registerUser_WithValidData_ShouldReturnUserResponse() throws Exception {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.registerUser(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getMessage()).isEqualTo("User registered successfully");

        // Verify interactions
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when passwords don't match")
    void registerUser_WithMismatchedPasswords_ShouldThrowConflictException() {
        // Arrange
//        validRequest.setConfirmPassword("differentPassword");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Passwords do not match");

        // Verify no database interactions
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when username already exists")
    void registerUser_WithExistingUsername_ShouldThrowConflictException() {
        // Arrange
        User existingUser = new User();
        existingUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username already exists");

        // Verify
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}