package com.cyberscale.backend.dto;

public record ExecutionRequest(
    String containerId,
    String command
) {}
