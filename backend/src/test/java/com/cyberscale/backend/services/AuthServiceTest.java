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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService.
 * Utilise Mockito pour simuler le UserRepository et tester la logique métier du service.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    // Mock le composant externe (la base de données via le repository)
    @Mock
    private UserRepository userRepository;

    // Injecte les mocks créés ci-dessus dans l'instance de AuthService
    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Initialisation des objets de test récurrents
        validRegisterRequest = new RegisterRequest("john.doe", "john@test.com", "password123");
        validLoginRequest = new LoginRequest("john.doe", "password123");
        mockUser = new User("john.doe", "john@test.com", "password123");
        mockUser.setId(1L);
    }

    // ===================================
    // TESTS pour registerUser()
    // ===================================

    @Test
    void registerUser_Success() {
        // Arrange : Configurer le mock pour simuler un nouvel utilisateur
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User resultUser = authService.registerUser(validRegisterRequest);

        // Assert
        assertNotNull(resultUser);
        assertEquals("john@test.com", resultUser.getEmail());

        // Verify : S'assurer que les vérifications d'existence et la sauvegarde ont eu lieu
        verify(userRepository, times(1)).existsByEmail(validRegisterRequest.email());
        verify(userRepository, times(1)).existsByUsername(validRegisterRequest.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailConflict() {
        // Arrange : Simuler que l'email existe déjà
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(true);
        
        // Act & Assert : Vérifier que l'exception est levée
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(validRegisterRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email déjà utilisé", exception.getReason());

        // Verify : S'assurer que la vérification de l'email a eu lieu, mais pas celle de l'username ni la sauvegarde
        verify(userRepository, times(1)).existsByEmail(validRegisterRequest.email());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_UsernameConflict() {
        // Arrange : Simuler que l'email est unique, mais l'username est déjà pris
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(validRegisterRequest.username())).thenReturn(true);

        // Act & Assert : Vérifier que l'exception est levée
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(validRegisterRequest);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Nom d'utilisateur déjà utilisé", exception.getReason());

        // Verify : S'assurer que les deux vérifications d'existence ont eu lieu, mais pas la sauvegarde
        verify(userRepository, times(1)).existsByEmail(validRegisterRequest.email());
        verify(userRepository, times(1)).existsByUsername(validRegisterRequest.username());
        verify(userRepository, never()).save(any(User.class));
    }

    // ===================================
    // TESTS pour loginUser()
    // ===================================

    @Test
    void loginUser_Success() {
        // Arrange : Simuler que l'utilisateur est trouvé avec le bon mot de passe
        when(userRepository.findByUsername(validLoginRequest.username())).thenReturn(Optional.of(mockUser));

        // Act
        User resultUser = authService.loginUser(validLoginRequest);

        // Assert
        assertNotNull(resultUser);
        assertEquals(mockUser.getUsername(), resultUser.getUsername());

        // Verify : S'assurer que la recherche a été effectuée
        verify(userRepository, times(1)).findByUsername(validLoginRequest.username());
    }

    @Test
    void loginUser_UserNotFound() {
        // Arrange : Simuler que l'utilisateur n'est pas trouvé
        when(userRepository.findByUsername(validLoginRequest.username())).thenReturn(Optional.empty());

        // Act & Assert : Vérifier que l'exception est levée
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.loginUser(validLoginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Nom d'utilisateur ou mot de passe incorrect", exception.getReason());

        // Verify
        verify(userRepository, times(1)).findByUsername(validLoginRequest.username());
    }

    @Test
    void loginUser_IncorrectPassword() {
        // Arrange : Simuler que l'utilisateur est trouvé, mais avec un mot de passe différent
        User userWithWrongPassword = new User("john.doe", "john@test.com", "wrongpassword");
        when(userRepository.findByUsername(validLoginRequest.username())).thenReturn(Optional.of(userWithWrongPassword));

        // Act & Assert : Vérifier que l'exception est levée
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.loginUser(validLoginRequest); // Le request a "password123"
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Nom d'utilisateur ou mot de passe incorrect", exception.getReason());

        // Verify
        verify(userRepository, times(1)).findByUsername(validLoginRequest.username());
    }
}