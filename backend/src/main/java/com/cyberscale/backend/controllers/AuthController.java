package com.cyberscale.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.models.User;
import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.services.AuthService;
import jakarta.validation.Valid; 
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Tâche F5: Endpoint POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) { 
        
        User newUser = authService.registerUser(registerRequest);

        return new ResponseEntity<>("Inscription réussie pour l'utilisateur: " + newUser.getEmail(), HttpStatus.CREATED);
    }

    // Tâche F5: Endpoint POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) { 
        
        User authenticatedUser = authService.loginUser(loginRequest);

        return ResponseEntity.ok(
            new LoginResponse(
                authenticatedUser.getEmail(), 
                "Connexion réussie !"
            )
        );
    }
    
    public static class LoginResponse {
        private String email;
        private String message;
        
        public LoginResponse(String email, String message) {
            this.email = email;
            this.message = message;
        }

        public String getEmail() {
            return email;
        }
        public String getMessage() {
            return message;
        }


    }
}