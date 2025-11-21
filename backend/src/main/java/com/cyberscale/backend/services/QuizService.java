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
import com.cyberscale.backend.dto.ResultsResponse;
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.Recommendation;
import com.cyberscale.backend.models.UserAnswer;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository; 
import com.cyberscale.backend.repositories.RecommendationRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;

@Service
public class QuizService {

    // On injecte le "Repository" pour pouvoir parler à la BDD
    @Autowired
    private QuizSessionRepository quizSessionRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerOptionRepository answerOptionRepository;
    @Autowired
    private UserAnswerRepository userAnswerRepository;
    @Autowired
    private RecommendationRepository recommendationRepository;
    
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

    public void saveUserAnswer(UserAnswerRequest request) {
        // 1. Vérifier que tout existe
        QuizSession session = quizSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        Question question = questionRepository.findById(request.questionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));

        AnswerOption selectedOption = answerOptionRepository.findById(request.answerOptionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réponse introuvable"));

        // 2. Créer et sauvegarder l'entité
        UserAnswer userAnswer = new UserAnswer(session, question, selectedOption);
        userAnswerRepository.save(userAnswer);
    }

    public ResultsResponse calculateAndGetResults(Long sessionId) {
        // 1. Récupérer la session
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        // 2. Récupérer toutes les réponses de l'utilisateur
        List<UserAnswer> answers = userAnswerRepository.findBySessionId(sessionId);

        /*if (answers.isEmpty()) {
            // Pas de réponse ? Score 0.
            return new ResultsResponse(0.0, 0.0, Collections.emptyList());
        }*/

        // 3. Calculer les scores (Théorie vs Technique)
        int theoryTotal = 0;
        int theoryCorrect = 0;
        int techTotal = 0;
        int techCorrect = 0;

        for (UserAnswer answer : answers) {
            Question q = answer.getQuestion();
            boolean isCorrect = answer.getSelectedOption().getIsCorrect();

            if (q.getCategorie() == Question.categorieQuestion.THEORY) {
                theoryTotal++;
                if (isCorrect) theoryCorrect++;
            } else {
                techTotal++;
                if (isCorrect) techCorrect++;
            }
        }

        // Règle de 3 pour avoir un score sur 10
        Double finalScoreTheory = (theoryTotal == 0) ? 0.0 : ((double) theoryCorrect / theoryTotal) * 10.0;
        Double finalScoreTechnique = (techTotal == 0) ? 0.0 : ((double) techCorrect / techTotal) * 10.0;

        // 4. Sauvegarder les scores en BDD (Mise à jour de la session)
        session.setFinalScoreTheory(finalScoreTheory);
        session.setFinalScoreTechnique(finalScoreTechnique);
        quizSessionRepository.save(session);

        // 5. Sélectionner les recommandations (Logique F4 simple)
        String targetProfile;
        if (finalScoreTechnique < 5) {
            targetProfile = "LOW_TECH";
        } else if (finalScoreTheory < 5) {
            targetProfile = "LOW_THEORY";
        } else {
            targetProfile = "HIGH_ALL";
        }

        List<Recommendation> recommendations = recommendationRepository.findByTargetProfile(targetProfile);

        // 6. Retourner le tout
        return new ResultsResponse(finalScoreTheory, finalScoreTechnique, recommendations);
    }

}