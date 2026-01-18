package com.cyberscale.backend.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.models.UserAnswer;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.ExamSessionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;
import com.cyberscale.backend.repositories.UserRepository;

import jakarta.transaction.Transactional;
import java.util.Map;

/**
 * Service gérant le cycle de vie des examens.
 * Il gère le démarrage, la soumission des réponses et le calcul final des scores.
 */
@Service
public class ExamService {

    @Autowired private ExamSessionRepository examSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;
    @Autowired private UserRepository userRepository;


    /**
     * Démarre une nouvelle session d'examen pour un candidat.
     * @param candidateName Nom affiché du candidat.
     * @param userId ID de l'utilisateur (optionnel).
     * @param examRef Référence de l'examen (ex: "CEH").
     * @return La session créée.
     */
    public ExamSession startExam(String candidateName, Long userId, String examRef) {
        ExamSession session = new ExamSession();
        session.setCandidateName(candidateName);
        session.setExamRef(examRef); 
        session.setEndTime(LocalDateTime.now().plusMinutes(30));

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            session.setUser(user);
        }
        return examSessionRepository.save(session);
    }

    /**
     * Enregistre une réponse à une question d'examen.
     * @param sessionId  ID de la session.
     * @param questionId ID de la question.
     * @param optionId   ID de la réponse choisie.
     * @throws ResponseStatusException Si le temps est écoulé ou les entités introuvables.
     */
    public void submitExamAnswer(Long sessionId, Long questionId, Long optionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session d'examen introuvable"));

        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'examen est terminé ! Réponse refusée.");
        }

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
        AnswerOption selectedOption = answerOptionRepository.findById(optionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option introuvable"));

        UserAnswer answer = new UserAnswer();
        answer.setExamSession(session);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setAnsweredAt(LocalDateTime.now());
        
        userAnswerRepository.save(answer);
    }

    /**
     * Clôture l'examen, calcule le score et estime la probabilité de réussite à la vraie certification.
     * @param sessionId ID de la session à terminer.
     * @return La session mise à jour avec les scores.
     */
    @Transactional
    public ExamSession finishExam(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        List<UserAnswer> answers = userAnswerRepository.findAll(); 
        int totalScore = 0;
        int maxScore = 0;

        for (UserAnswer ans : answers) {
            if (ans.getExamSession() != null && session.getId().equals(ans.getExamSession().getId())) {
                Question q = ans.getQuestion();
                maxScore += q.getPointsWeight();
                if (ans.getSelectedOption().getIsCorrect()) {
                    totalScore += q.getPointsWeight();
                }
            } 
        }
        
        session.setFinalScore(totalScore);
        session.setMaxPossibleScore(maxScore);

        if (maxScore > 0) {
            double userPercent = (double) totalScore / maxScore;
            
            double officialThreshold = 0.70; 
            if ("Security+".equals(session.getExamRef())) officialThreshold = 0.83; 
            if ("CEH".equals(session.getExamRef())) officialThreshold = 0.75; 
            
            int probability = (int) ((userPercent / officialThreshold) * 80.0);
            
            probability = Math.min(99, Math.max(0, probability));
            
            session.setSuccessProbability(probability);
        } else {
            session.setSuccessProbability(0);
        }

        if (LocalDateTime.now().isBefore(session.getEndTime())) {
            session.setEndTime(LocalDateTime.now());
        }

        return examSessionRepository.save(session);
    }

    /**
     * Récupère une liste aléatoire de questions pour l'examen.
     * @param sessionId ID de la session.
     * @return Liste de questions mélangées.
     */
    public List<Question> getExamQuestions(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        List<Question> questions;
        if (session.getExamRef() != null) {
            questions = questionRepository.findByExamRef(session.getExamRef());
        } else {
            questions = questionRepository.findAll();
        }
        
        java.util.Collections.shuffle(questions);
        return questions.stream().limit(20).toList();
    }

    /**
     * Renvoie l'état actuel de l'examen (Temps restant, progression).
     * @param sessionId ID de la session.
     * @return Map contenant les métadonnées.
     */
    public Map<String, Object> getExamStatus(Long sessionId) {
    ExamSession session = examSessionRepository.findById(sessionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

    long secondsLeft = java.time.Duration.between(LocalDateTime.now(), session.getEndTime()).getSeconds();
    if (secondsLeft < 0) secondsLeft = 0;

    long answeredCount = userAnswerRepository.countByExamSessionId(sessionId);

    return Map.of(
        "secondsLeft", secondsLeft,
        "isFinished", session.getFinalScore() != null,
        "examRef", session.getExamRef() != null ? session.getExamRef() : "EXAM",
        "currentIndex", answeredCount
    );
}
    
}