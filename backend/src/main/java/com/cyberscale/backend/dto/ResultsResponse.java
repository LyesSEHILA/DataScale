package com.cyberscale.backend.dto;

import com.cyberscale.backend.models.Recommendation;
import java.util.List;

/**
 * DTO contenant les résultats calculés d'une session de Quiz.
 * Il est renvoyé au frontend pour afficher les jauges de compétences
 * et proposer des contenus pédagogiques adaptés.
 */
public record ResultsResponse(
    Double scoreTheory,   
    Double scoreTechnique,
    List<Recommendation> recommendations
) {}