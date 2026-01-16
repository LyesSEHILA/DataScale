package com.cyberscale.backend.services;

import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import java.util.List;

/**
 * Interface définissant la stratégie de génération des questions.
 * Permet d'injecter différents types de générateurs.
 */
public interface QuestionGenerator {
    
    /**
     * Génère la liste de questions adaptées à la session.
     * @param session La session qui contient les scores d'auto-évaluation.
     * @return La liste mélangée de questions.
     */
    List<Question> generate(QuizSession session);
}