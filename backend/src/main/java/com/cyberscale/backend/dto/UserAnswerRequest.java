package com.cyberscale.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO utilisé pour soumettre une réponse utilisateur lors d'un Quiz.
 */
public record UserAnswerRequest(
    @NotNull Long sessionId,
    @NotNull Long questionId,
    @NotNull Long answerOptionId
) {}