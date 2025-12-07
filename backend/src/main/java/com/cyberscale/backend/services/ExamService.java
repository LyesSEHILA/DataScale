package com.cyberscale.backend.services;

import com.cyberscale.backend.models.*;
import com.cyberscale.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamService {

    @Autowired private ExamSessionRepository examSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;

    /**
     * Démarrer un examen
     */
    public ExamSession startExam(String candidateName) {
        ExamSession session = new ExamSession();
        session.setCandidateName(candidateName);
        // Règle : Examen de 30 minutes par exemple
        session.setEndTime(LocalDateTime.now().plusMinutes(30));
        return examSessionRepository.save(session);
    }

    /**
     * Soumettre une réponse avec VALIDATION TEMPORELLE
     */
    public void submitExamAnswer(Long sessionId, Long questionId, Long optionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session d'examen introuvable"));

        // 1. Règle Stricte : Check Time
        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'examen est terminé ! Réponse refusée.");
        }

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
        AnswerOption selectedOption = answerOptionRepository.findById(optionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option introuvable"));

        // Sauvegarde (en utilisant le champ examSession ajouté à UserAnswer)
        UserAnswer answer = new UserAnswer();
        answer.setExamSession(session);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setAnsweredAt(LocalDateTime.now());
        
        userAnswerRepository.save(answer);
    }

    /**
     * Terminer l'examen et Calculer le Score Pondéré
     */
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
        
        if (LocalDateTime.now().isBefore(session.getEndTime())) {
            session.setEndTime(LocalDateTime.now());
        }

        return examSessionRepository.save(session);
    }
}