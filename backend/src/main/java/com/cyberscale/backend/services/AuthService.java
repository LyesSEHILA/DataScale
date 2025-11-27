package com.cyberscale.backend.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.models.User;
import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.repositories.UserRepository;

// Le nom du service doit être AuthServiceV2 pour répondre à la demande de nouveau nom
@Service 
public class AuthService { // Classe renommée

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(RegisterRequest request) {
        
        // 1. Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(request.email())) { 
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Email déjà utilisé"
            );
        }

        // 2. Vérification de l'unicité du nom d'utilisateur
        if (userRepository.existsByUsername(request.username())) { 
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Nom d'utilisateur déjà utilisé"
            );
        }
        
        String plainPassword = request.password(); 
        
        // Construction correcte de l'objet User
        User newUser = new User(
            request.username(),  // Champ Username
            request.email(),     // Champ Email
            plainPassword        // Le mot de passe est stocké en clair (temporaire)
        );

        return userRepository.save(newUser);
    }

    public User loginUser(LoginRequest request) {
        
        // Recherche l'utilisateur par son nom d'utilisateur (username)
        User user = userRepository.findByUsername(request.username()) 
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Nom d'utilisateur ou mot de passe incorrect"
            ));
        // Remarque: Comparaison en clair (pas de hachage)
        if (!user.getPassword().equals(request.password())) { 
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Nom d'utilisateur ou mot de passe incorrect"
            );
        }

        return user;
    }
}