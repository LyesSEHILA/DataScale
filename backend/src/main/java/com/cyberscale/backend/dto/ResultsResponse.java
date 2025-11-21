package com.cyberscale.backend.dto;

import com.cyberscale.backend.models.Recommendation;
import java.util.List;

public record ResultsResponse(
    Double scoreTheory,    // Score sur 10 ou 100
    Double scoreTechnique, // Score sur 10 ou 100
    List<Recommendation> recommendations
) {}