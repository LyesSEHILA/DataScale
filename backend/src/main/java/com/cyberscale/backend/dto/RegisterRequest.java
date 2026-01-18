package com.cyberscale.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la requête d'inscription d'un nouvel utilisateur.
 * Capture les informations essentielles lors de la création de compte
 * et applique les règles de validation.
 */
public record RegisterRequest(

    @NotBlank(message = "Le nom d'utilisateur est requis")
    String username,
    
    @NotBlank(message = "L'email est requis")
    String email,

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password
  
) {}