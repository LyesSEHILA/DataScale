package com.cyberscale.backend.dto;

import java.util.List;

/**
 * Data Transfer Object représentant les données du tableau de bord utilisateur.
 */
public record DashboardResponse(
    Double averageTheory,
    Double averageTechnique,
    Integer totalQuizzes,
    List<String> certifications,
    List<RecommendationDTO> resources
) {
    public record RecommendationDTO(String title, String type, String url) {}
}