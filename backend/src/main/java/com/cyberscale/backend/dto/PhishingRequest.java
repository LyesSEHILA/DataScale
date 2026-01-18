package com.cyberscale.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO utilisé lorsqu'un utilisateur clique sur un élément
 * dans l'interface de simulation de Phishing.
 */
public record PhishingRequest(
    @JsonProperty("scenarioId") String scenarioId,
    @JsonProperty("elementId") String elementId
) {}