package com.cyberscale.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilisé pour la requête d'authentification.
 * Transporte les identifiants nécessaires pour qu'un utilisateur
 * puisse se connecter à la plateforme.
 * </p>
 */
public record LoginRequest(
    @NotBlank(message = "L'email est requis")
    String email,

    @NotBlank(message = "Le mot de passe est requis")
    String password
) {}