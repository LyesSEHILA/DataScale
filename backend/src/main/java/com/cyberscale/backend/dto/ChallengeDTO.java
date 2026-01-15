package com.cyberscale.backend.dto;

/**
 * DTO représentant un Challenge tel qu'affiché dans la liste des épreuves.
 * Contient les informations du challenge (sans le flag secret)
 * ainsi que l'état de progression de l'utilisateur.
 */
public record ChallengeDTO(
    String id,
    String title,       
    String description,
    Integer points,
    String difficulty,   
    boolean isValidated 
) {}