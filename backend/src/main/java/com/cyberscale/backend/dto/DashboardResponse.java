package com.cyberscale.backend.dto;

import java.util.List;

public record DashboardResponse(
    Double averageTheory,
    Double averageTechnique,
    Integer totalQuizzes,
    List<String> certifications,
    List<RecommendationDTO> resources
) {
    public record RecommendationDTO(String title, String type, String url) {}
}