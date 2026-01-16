package com.cyberscale.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO utilisé pour soumettre une réponse lors d'un Examen de certification blanche.
 * Transporte les identifiants nécessaires pour lier la réponse à une session d'examen active,
 * une question spécifique et l'option choisie par le candidat.
 */
public record ExamAnswerRequest(
    @NotNull Long sessionId,
    @NotNull Long questionId,
    @NotNull Long optionId
) {}