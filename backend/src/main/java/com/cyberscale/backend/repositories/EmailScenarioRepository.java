package com.cyberscale.backend.repositories;

import com.cyberscale.backend.models.EmailScenario;
import com.cyberscale.backend.models.IQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface d'acces aux donnees pour les scenarios de Phishing.
 */
@Repository
public interface EmailScenarioRepository extends JpaRepository<EmailScenario, Long> {
    
    List<EmailScenario> findByDifficulty(IQuestion.DifficultyQuestion difficulty);
}