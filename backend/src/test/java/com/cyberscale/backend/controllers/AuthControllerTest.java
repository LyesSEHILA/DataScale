package com.cyberscale.backend.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests unitaires pour AuthController.
 * @WebMvcTest charge uniquement la couche contrôleur, ce qui est plus rapide.
 * Le AuthService est injecté en tant que @MockBean pour simuler son comportement.
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // Permet de simuler des requêtes HTTP

    @Autowired
    private ObjectMapper objectMapper; // Convertit les objets Java en JSON

    @MockitoBean 
    private AuthService authService;

    private final String BASE_URL = "/api/auth";

    // --- Tests pour /api/auth/register ---

    /**
     * Teste le cas de succès de l'enregistrement.
     * Vérifie que le statut 201 CREATED est retourné.
     */
    @Test
    void testRegisterUser_Success() throws Exception {
        // Arrange (Préparation)
        RegisterRequest request = new RegisterRequest("testuser", "test@email.com", "password123");
        
        // Simule l'utilisateur qui sera retourné par le service
        User mockUser = new User("testuser", "test@email.com", "password123");
        mockUser.setId(1L);

        // Configure le mock : QUAND authService.registerUser EST APPELÉ, ALORS RETOURNER mockUser
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(mockUser);

        // Act (Action) & Assert (Vérification)
        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated()) // Vérifie le statut 201
            .andExpect(content().string("Inscription réussie pour l'utilisateur: test@email.com"));

        // Verify (Vérification que le service a été appelé)
        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
    }

    /**
     * Teste l'échec de l'enregistrement si l'email existe déjà.
     * Vérifie que le statut 409 CONFLICT est retourné.
     */
    @Test
    void testRegisterUser_Conflict() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("existinguser", "existing@email.com", "password123");
        
        // Configure le mock : QUAND authService.registerUser EST APPELÉ, ALORS LEVER UNE EXCEPTION
        when(authService.registerUser(any(RegisterRequest.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé"));

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict()); // Vérifie le statut 409
            
        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
    }

    /**
     * Teste l'échec de l'enregistrement si les données sont invalides (ex: mot de passe trop court).
     * Vérifie que le statut 400 BAD REQUEST est retourné (grâce à @Valid).
     */
    @Test
    void testRegisterUser_InvalidData() throws Exception {
        // Arrange
        // Supposons que le DTO a une validation @Size(min=8) sur le mot de passe
        RegisterRequest request = new RegisterRequest("testuser", "test@email.com", "123"); // Mot de passe invalide

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Vérifie le statut 400

        // Vérifie que le service n'a JAMAIS été appelé car la validation a échoué avant
        verify(authService, never()).registerUser(any());
    }

    // --- Tests pour /api/auth/login ---

    /**
     * Teste le cas de succès de la connexion.
     * Vérifie que le statut 200 OK est retourné et que le JSON contient l'email.
     */
    @Test
    void testLoginUser_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");
        User mockUser = new User("testuser", "test@email.com", "password123");

        // Configure le mock
        when(authService.loginUser(any(LoginRequest.class))).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk()) // Vérifie le statut 200
            .andExpect(jsonPath("$.email").value("test@email.com")) // Vérifie le champ 'email' dans le JSON
            .andExpect(jsonPath("$.message").value("Connexion réussie !"));

        verify(authService, times(1)).loginUser(any(LoginRequest.class));
    }

    /**
     * Teste l'échec de la connexion si les identifiants sont incorrects.
     * Vérifie que le statut 401 UNAUTHORIZED est retourné.
     */
    @Test
    void testLoginUser_Unauthorized() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        // Configure le mock
        when(authService.loginUser(any(LoginRequest.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"));

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized()); // Vérifie le statut 401

        verify(authService, times(1)).loginUser(any(LoginRequest.class));
    }

    /**
     * Teste l'échec de la connexion si les données sont invalides (ex: username manquant).
     * Vérifie que le statut 400 BAD REQUEST est retourné (grâce à @Valid).
     */
    @Test
    void testLoginUser_InvalidData() throws Exception {
        // Arrange
        // Supposons que le DTO a une validation @NotNull sur le username
        LoginRequest request = new LoginRequest(null, "password123"); // Username manquant

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Vérifie le statut 400

        // Vérifie que le service n'a JAMAIS été appelé
        verify(authService, never()).loginUser(any());
    }
}