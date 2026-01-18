package com.cyberscale.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cyberscale.backend.models.UserChallenge;

/**
 * Interface d'acces aux donnees pour la table de jointure user_challenges.
 */
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    List<UserChallenge> findByUserId(Long userId);
    boolean existsByUserIdAndChallengeId(Long userId, String challengeId);
}