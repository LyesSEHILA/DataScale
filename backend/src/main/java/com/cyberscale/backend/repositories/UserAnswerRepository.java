package com.cyberscale.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.UserAnswer;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    // Nous en aurons besoin pour la F3 (calcul des résultats)
    List<UserAnswer> findBySessionId(Long sessionId);

    /**
     * Méthode du Compteur : Compte le nombre de réponses correctes 
     * soumises historiquement pour une question donnée.
     */
    long countByQuestionIdAndIsCorrectTrue(Long questionId);
}