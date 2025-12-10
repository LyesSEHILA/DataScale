package com.cyberscale.backend.dto;

import java.time.LocalDateTime;

public record HistoryDTO(
    Long id,
    String type,        // "QUIZ" ou "EXAMEN"
    String title,       // "Positionnement" ou "CEH Simulator"
    Integer score,      // Score obtenu
    Integer maxScore,   // Score max (10 ou 100)
    LocalDateTime date, // Date de passage
    String status       // "Réussi", "Échoué", "Terminé"
) {}