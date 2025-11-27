package com.cyberscale.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(

    @NotNull(message = "Le nom d'utilisateur est requis")
    String username,
    
    @NotNull(message = "L'email est requis")
    String email,

    @NotNull(message = "Le mot de passe est requis")
    @Min(value = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password
  
) {}