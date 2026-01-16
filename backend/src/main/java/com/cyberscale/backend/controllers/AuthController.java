package com.cyberscale.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.services.AuthService;

import jakarta.validation.Valid;

/**
 * Controleur REST gérant l'authentification et l'inscription.
 * Il expose les endpoints publics pour :
 * - Créer un nouveau compte.
 * - Se connecter et récupérer ses informations.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscrit un nouvel utilisateur.
     * @param registerRequest DTO contenant username, email, password.
     * @return code HTTP 201.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) { 
        
        User newUser = authService.registerUser(registerRequest);

        return new ResponseEntity<>("Inscription réussie pour l'utilisateur: " + newUser.getEmail(), HttpStatus.CREATED);
    }

    /**
     * Authentifie un utilisateur.
     * @param loginRequest DTO contenant email et password.
     * @return code HTTP 200 et les informations sur l'utilisateurs.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) { 
        
        User authenticatedUser = authService.loginUser(loginRequest);

        return ResponseEntity.ok(
            new LoginResponse(
                authenticatedUser.getEmail(),
                authenticatedUser.getUsername(), 
                authenticatedUser.getId(),       
                "Connexion réussie !"
            )
        );
    }
    
    public static class LoginResponse {
        private String email;
        private String username; 
        private Long id;         
        private String message;
        
        public LoginResponse(String email, String username, Long id, String message) {
            this.email = email;
            this.username = username;
            this.id = id;
            this.message = message;
        }

        public String getEmail() { return email; }
        public String getUsername() { return username; } 
        public Long getId() { return id; }               
        public String getMessage() { return message; }
    }
}