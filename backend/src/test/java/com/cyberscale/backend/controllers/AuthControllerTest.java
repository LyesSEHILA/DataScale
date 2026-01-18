package com.cyberscale.backend.controllers;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.config.SecurityConfig;
import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)

class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // Permet de simuler des requêtes HTTP

    @Autowired
    private ObjectMapper objectMapper; // Convertit les objets Java en JSON

    @MockitoBean 
    private AuthService authService;

    private final String BASE_URL = "/api/auth";


    /**
     * Teste le cas de succès de l'enregistrement.
     * Vérifie que le statut 201 CREATED est retourné.
     */
    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "test@email.com", "password123");
        User mockUser = new User("testuser", "test@email.com", "password123");
        mockUser.setId(1L);

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(mockUser);

        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated()) 
            .andExpect(content().string("Inscription réussie pour l'utilisateur: test@email.com"));

        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
    }

    /**
     * Teste l'échec de l'enregistrement si l'email existe déjà.
     * Vérifie que le statut 409 CONFLICT est retourné.
     */
    @Test
    void testRegisterUser_Conflict() throws Exception {
        RegisterRequest request = new RegisterRequest("existinguser", "existing@email.com", "password123");
        
        when(authService.registerUser(any(RegisterRequest.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé"));

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict()); 
            
        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
    }

    /**
     * Teste l'échec de l'enregistrement si les données sont invalides (ex: mot de passe trop court).
     * Vérifie que le statut 400 BAD REQUEST est retourné (grâce à @Valid).
     */
    @Test
    void testRegisterUser_InvalidData() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "test@email.com", "123"); // Mot de passe invalide

        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Vérifie le statut 400

        verify(authService, never()).registerUser(any());
    }


    /**
     * Teste le cas de succès de la connexion.
     * Vérifie que le statut 200 OK est retourné et que le JSON contient l'email.
     */
    @Test
    void testLoginUser_Success() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User mockUser = new User("testuser", "test@email.com", "password123");

        when(authService.loginUser(any(LoginRequest.class))).thenReturn(mockUser);

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
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
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(authService.loginUser(any(LoginRequest.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"));

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
        LoginRequest request = new LoginRequest(null, "password123");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
        verify(authService, never()).loginUser(any());
    }
}