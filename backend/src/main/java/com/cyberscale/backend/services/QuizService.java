package com.cyberscale.backend.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;

@Service
public class QuizService {

    // On injecte le "Repository" pour pouvoir parler à la BDD
    @Autowired
    private QuizSessionRepository quizSessionRepository;
    @Autowired
    private QuestionRepository questionRepository;
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

    public List<Question> getQuestionsForSession(Long sessionId) {
        // 1. Récupérer la session
        QuizSession session = quizSessionRepository.findById(sessionId)
.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session non trouvée"));
        // 2. Déterminer le niveau
        boolean isTheoryAdvanced = session.getSelfEvalTheory() > 5;
        boolean isTechAdvanced = session.getSelfEvalTechnique() > 5;

        List<Question> questions = new ArrayList<>();

        // 3. Sélectionner les questions THÉORIE
        if (isTheoryAdvanced) {
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.THEORY, Question.difficultyQuestion.MEDIUM));
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.THEORY, Question.difficultyQuestion.HARD));
        } else {
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.THEORY, Question.difficultyQuestion.EASY));
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.THEORY, Question.difficultyQuestion.MEDIUM));
        }

        // 4. Sélectionner les questions TECHNIQUE
        if (isTechAdvanced) {
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.TECHNIQUE, Question.difficultyQuestion.MEDIUM));
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.TECHNIQUE, Question.difficultyQuestion.HARD));
        } else {
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.TECHNIQUE, Question.difficultyQuestion.EASY));
            questions.addAll(questionRepository.findByCategorieAndDifficulty(Question.categorieQuestion.TECHNIQUE, Question.difficultyQuestion.MEDIUM));
        }

        // 5. Mélanger et Limiter
        Collections.shuffle(questions);
        return questions.stream().limit(10).collect(Collectors.toList());
    }

}