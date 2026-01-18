package com.cyberscale.backend.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder; // Import important
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.dto.LoginRequest;
import com.cyberscale.backend.dto.RegisterRequest;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.UserRepository;

/**
 * Service responsable de l'authentification et de l'inscription des utilisateurs.
 * Il gère la vérification des doublons (email/username), le hachage des mots de passe
 * et la validation des identifiants lors de la connexion.
 */
@Service 
public class AuthService { 

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur dans la base de données.
     * @param request DTO contenant les informations d'inscription.
     * @return L'utilisateur créé et sauvegardé.
     * @throws ResponseStatusException si l'email ou le username existe déjà.
     */
    public User registerUser(RegisterRequest request) {
        
        if (userRepository.existsByEmail(request.email())) { 
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        if (userRepository.existsByUsername(request.username())) { 
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nom d'utilisateur déjà utilisé");
        }
        
        String hashedPassword = passwordEncoder.encode(request.password());
        
        User newUser = new User(
            request.username(),
            request.email(),
            hashedPassword
        );

        return userRepository.save(newUser);
    }

    /**
     * Authentifie un utilisateur à partir de son email et de son mot de passe.
     * @param request DTO contenant les identifiants de connexion.
     * @return L'utilisateur authentifié si les informations sont correctes.
     * @throws ResponseStatusException si l'utilisateur n'existe pas ou le mot de passe est incorrect.
     */
    public User loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()) 
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) { 
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
        }

        return user;
    }
}