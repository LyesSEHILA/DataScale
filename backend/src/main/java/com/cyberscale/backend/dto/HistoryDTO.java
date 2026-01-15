package com.cyberscale.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO représentant une entrée dans l'historique d'activité de l'utilisateur.
 * Est utilisé pour afficher une liste des événements passés,
 * qu'il s'agisse de Quiz d'entraînement ou d'Examens de certification blanche.
 */
public record HistoryDTO(
    Long id,
    String type,        
    String title,       
    Integer score,      
    Integer maxScore,   
    LocalDateTime date, 
    String status     
) {}