package com.cyberscale.backend.services;

import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.models.QuizSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuizService {

    // On injecte le "Repository" pour pouvoir parler à la BDD
    @Autowired
    private QuizSessionRepository quizSessionRepository;

    /**
     * Crée et sauvegarde une nouvelle session de quiz basée sur la demande d'onboarding.
     * C'est le DoD #4.
     */
    public QuizSession createSession(OnboardingRequest request) {
        
        QuizSession newSession = new QuizSession();
        newSession.setAge(request.age());
        newSession.setSelfEvalTheory(request.selfEvalTheory());
        newSession.setSelfEvalTechnique(request.selfEvalTechnique());

        // Sauvegarde la nouvelle session en BDD et retourne l'objet sauvegardé (avec son ID)
        return quizSessionRepository.save(newSession);
    }

    // (Plus tard, tu ajouteras les méthodes getQuestions(), processAnswer() ici...)
}