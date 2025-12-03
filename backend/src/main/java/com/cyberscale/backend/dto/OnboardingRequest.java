package com.cyberscale.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OnboardingRequest(
    
    @NotNull(message = "L'âge est requis")
    @Min(value = 1, message = "L'âge doit être supérieur à 0")
    Long age,

    @NotNull(message = "L'évaluation théorique est requise")
    @Min(value = 0, message = "L'évaluation doit être entre 1 et 10")
    @Max(value = 10, message = "L'évaluation doit être entre 1 et 10")
    Long selfEvalTheory,

    @NotNull(message = "L'évaluation technique est requise")
    @Min(value = 0, message = "L'évaluation doit être entre 1 et 10")
    @Max(value = 10, message = "L'évaluation doit être entre 1 et 10")
    Long selfEvalTechnique
    
) {}