package com.cyberscale.backend.services;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.dto.ResultsResponse;
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.IQuestion;
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

    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;
    @Autowired private RecommendationRepository recommendationRepository;
    
    // INJECTION DU GÉNÉRATEUR (C'est lui qui va être testé désormais)
    @Autowired 
    private QuestionGenerator questionGenerator;

    /**
     * F1 : Créer une session
     */
    public QuizSession createSession(OnboardingRequest request) {
        QuizSession newSession = new QuizSession();
        newSession.setAge(request.age());
        newSession.setSelfEvalTheory(request.selfEvalTheory());
        newSession.setSelfEvalTechnique(request.selfEvalTechnique());
        return quizSessionRepository.save(newSession);
    }

    /**
     * F2 : Récupérer les questions (Délégué au générateur)
     */
    public List<Question> getQuestionsForSession(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        return questionGenerator.generate(session);
    }

    /**
     * F2 : Sauvegarder la réponse de l'utilisateur
     */
    public void saveUserAnswer(UserAnswerRequest request) {
        QuizSession session = quizSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));
        Question question = questionRepository.findById(request.questionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
        AnswerOption selectedOption = answerOptionRepository.findById(request.answerOptionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réponse introuvable"));

        UserAnswer userAnswer = new UserAnswer(session, question, selectedOption);
        userAnswerRepository.save(userAnswer);
    }

    /**
     * F3/F4 : Calculer les résultats et renvoyer les recommandations
     */
    public ResultsResponse calculateAndGetResults(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        List<UserAnswer> answers = userAnswerRepository.findBySessionId(sessionId);

        if (answers.isEmpty()) {
            return new ResultsResponse(0.0, 0.0, Collections.emptyList());
        }

        int theoryTotal = 0; int theoryCorrect = 0;
        int techTotal = 0; int techCorrect = 0;

        for (UserAnswer answer : answers) {
            Question q = answer.getQuestion();
            // Vérification null pour éviter les crashs
            if (answer.getSelectedOption() != null) {
                boolean isCorrect = answer.getSelectedOption().getIsCorrect();
                
                // Comparaison avec l'Enum de l'interface IQuestion
                if (q.getCategorie() == IQuestion.CategorieQuestion.THEORY) {
                    theoryTotal++;
                    if (isCorrect) theoryCorrect++;
                } else {
                    techTotal++;
                    if (isCorrect) techCorrect++;
                }
            }
        }

        Double finalScoreTheory = (theoryTotal == 0) ? 0.0 : ((double) theoryCorrect / theoryTotal) * 10.0;
        Double finalScoreTechnique = (techTotal == 0) ? 0.0 : ((double) techCorrect / techTotal) * 10.0;

        session.setFinalScoreTheory(finalScoreTheory);
        session.setFinalScoreTechnique(finalScoreTechnique);
        quizSessionRepository.save(session);

        String targetProfile;
        if (finalScoreTechnique < 5) targetProfile = "LOW_TECH";
        else if (finalScoreTheory < 5) targetProfile = "LOW_THEORY";
        else targetProfile = "HIGH_ALL";

        List<Recommendation> recommendations = recommendationRepository.findByTargetProfile(targetProfile);
        return new ResultsResponse(finalScoreTheory, finalScoreTechnique, recommendations);
    }
}