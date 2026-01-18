package com.cyberscale.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.Recommendation;

/**
 * Interface d'acces aux donnees pour les recommandations pedagogiques.
 */
@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByTargetProfile(String targetProfile);
}