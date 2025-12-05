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
import org.springframework.security.crypto.password.PasswordEncoder; // Import nécessaire
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // <--- AJOUT : On mock l'encodeur

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("john.doe", "john@test.com", "password123");
        validLoginRequest = new LoginRequest("john.doe", "password123");
        // On simule un utilisateur déjà en base (avec un mot de passe haché)
        mockUser = new User("john.doe", "john@test.com", "encodedPassword123");
        mockUser.setId(1L);
    }

    // ===================================
    // TESTS pour registerUser()
    // ===================================

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(false);
        // On simule le hachage du mot de passe
        when(passwordEncoder.encode(validRegisterRequest.password())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User resultUser = authService.registerUser(validRegisterRequest);

        // Assert
        assertNotNull(resultUser);
        
        // Verify
        verify(passwordEncoder, times(1)).encode(validRegisterRequest.password()); // Vérifie qu'on a bien haché
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailConflict() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(true);
        
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(validRegisterRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email déjà utilisé", exception.getReason());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_UsernameConflict() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(validRegisterRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Nom d'utilisateur déjà utilisé", exception.getReason());
    }

    // ===================================
    // TESTS pour loginUser()
    // ===================================

    @Test
    void loginUser_Success() {
        // Arrange
        when(userRepository.findByUsername(validLoginRequest.username())).thenReturn(Optional.of(mockUser));
        // On dit à l'encodeur : "Si on te donne 'password123' et le hash, dis que c'est bon"
        when(passwordEncoder.matches(validLoginRequest.password(), mockUser.getPassword())).thenReturn(true);

        // Act
        User resultUser = authService.loginUser(validLoginRequest);

        // Assert
        assertNotNull(resultUser);
        assertEquals(mockUser.getUsername(), resultUser.getUsername());
    }

    @Test
    void loginUser_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(validLoginRequest.username())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.loginUser(validLoginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        // CORRECTION : On attend le nouveau message sécurisé
        assertEquals("Identifiants incorrects", exception.getReason());
    }

    @Test
    void loginUser_IncorrectPassword() {
        // Arrange
        when(userRepository.findByUsername(validLoginRequest.username())).thenReturn(Optional.of(mockUser));
        // On dit à l'encodeur : "Le mot de passe ne correspond pas"
        when(passwordEncoder.matches(validLoginRequest.password(), mockUser.getPassword())).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.loginUser(validLoginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        // CORRECTION : On attend le nouveau message sécurisé
        assertEquals("Identifiants incorrects", exception.getReason());
    }
}