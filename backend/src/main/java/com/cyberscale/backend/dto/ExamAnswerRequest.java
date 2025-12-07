package com.cyberscale.backend.dto;

import jakarta.validation.constraints.NotNull;

public record ExamAnswerRequest(
    @NotNull Long sessionId,
    @NotNull Long questionId,
    @NotNull Long optionId
) {}