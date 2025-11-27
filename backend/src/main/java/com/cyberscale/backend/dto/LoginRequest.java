package com.cyberscale.backend.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
    @NotNull(message = "Le nom d'utilisateur est requis")
    String username,
    
    @NotNull(message = "Le mot de passe est requis")
    String password
    
) {}