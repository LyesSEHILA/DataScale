package com.cyberscale.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UserAnswerRequest(
    @NotNull Long sessionId,
    @NotNull Long questionId,
    @NotNull Long answerOptionId
) {}