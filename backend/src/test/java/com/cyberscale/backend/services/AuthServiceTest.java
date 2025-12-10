package com.cyberscale.backend.services;

import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("john.doe", "john@test.com", "password123");
        
        // CORRECTION : On utilise l'email pour le login
        validLoginRequest = new LoginRequest("john@test.com", "password123");
        
        mockUser = new User("john.doe", "john@test.com", "encodedPassword123");
        mockUser.setId(1L);
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.password())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User resultUser = authService.registerUser(validRegisterRequest);

        assertNotNull(resultUser);
        verify(passwordEncoder).encode(validRegisterRequest.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailConflict() {
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(true);
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(validRegisterRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void registerUser_UsernameConflict() {
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(validRegisterRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    // --- CORRECTION DES TESTS DE LOGIN ---

    @Test
    void loginUser_Success() {
        // On cherche par EMAIL maintenant
        when(userRepository.findByEmail(validLoginRequest.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(validLoginRequest.password(), mockUser.getPassword())).thenReturn(true);

        User resultUser = authService.loginUser(validLoginRequest);

        assertNotNull(resultUser);
        assertEquals(mockUser.getEmail(), resultUser.getEmail());
    }

    @Test
    void loginUser_UserNotFound() {
        // On cherche par EMAIL
        when(userRepository.findByEmail(validLoginRequest.email())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.loginUser(validLoginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Identifiants incorrects", exception.getReason());
    }

    @Test
    void loginUser_IncorrectPassword() {
        // On cherche par EMAIL
        when(userRepository.findByEmail(validLoginRequest.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(validLoginRequest.password(), mockUser.getPassword())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.loginUser(validLoginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Identifiants incorrects", exception.getReason());
    }
}