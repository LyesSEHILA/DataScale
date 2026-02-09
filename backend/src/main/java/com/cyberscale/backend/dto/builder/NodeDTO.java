package com.cyberscale.backend.dto.builder;

public record NodeDTO(
    String id, 
    String type,   // "kali", "server", "db"
    String label
) {}