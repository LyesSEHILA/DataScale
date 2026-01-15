package com.cyberscale.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.Challenge;

/**
 * Interface d'acces aux donnees pour les Challenges.
 */
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, String> {
}