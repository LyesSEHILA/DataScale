package com.cyberscale.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PhishingRequest(
    @JsonProperty("scenarioId") String scenarioId,
    @JsonProperty("elementId") String elementId
) {}