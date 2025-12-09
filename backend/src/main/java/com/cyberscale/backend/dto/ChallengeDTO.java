package com.cyberscale.backend.dto;

public record ChallengeDTO(
    String id,
    String title,        // Correspond à "name" dans l'entité
    String description,
    Integer points,
    String difficulty,   // "FACILE", "MOYEN", "HARDCORE" (calculé ou stocké)
    boolean isValidated  // Si l'user l'a déjà réussi
) {}