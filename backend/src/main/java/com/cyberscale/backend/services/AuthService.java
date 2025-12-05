package com.cyberscale.backend.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder; // Import important
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.models.User;
import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.repositories.UserRepository;

@Service 
public class AuthService { 

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // On injecte l'encodeur

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request) {
        
        if (userRepository.existsByEmail(request.email())) { 
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }
        if (userRepository.existsByUsername(request.username())) { 
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nom d'utilisateur déjà utilisé");
        }
        
        // SÉCURITÉ : On hache le mot de passe avant de le sauvegarder
        String hashedPassword = passwordEncoder.encode(request.password());
        
        User newUser = new User(
            request.username(),
            request.email(),
            hashedPassword // On enregistre le hash crypté
        );

        return userRepository.save(newUser);
    }

    public User loginUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.username()) 
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects"));

        // SÉCURITÉ : On vérifie si le mot de passe correspond au hash
        if (!passwordEncoder.matches(request.password(), user.getPassword())) { 
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
        }

        return user;
    }
}