package com.cyberscale.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cyberscale.backend.models.UserChallenge;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    // Pour savoir quels challenges l'utilisateur a déjà validés
    List<UserChallenge> findByUserId(Long userId);
    
    // Pour vérifier si un challenge précis est déjà fait
    boolean existsByUserIdAndChallengeId(Long userId, String challengeId);
}