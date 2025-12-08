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

@Service
public class ExamService {

    @Autowired private ExamSessionRepository examSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;
    @Autowired private UserRepository userRepository;


    public ExamSession startExam(String candidateName, Long userId) {
        ExamSession session = new ExamSession();
        session.setCandidateName(candidateName);
        session.setEndTime(LocalDateTime.now().plusMinutes(30));

        // Si un ID est fourni, on lie l'utilisateur
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            session.setUser(user);
        }

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
        
        if (LocalDateTime.now().isBefore(session.getEndTime())) {
            session.setEndTime(LocalDateTime.now());
        }

        return examSessionRepository.save(session);
    }

    public List<Question> getExamQuestions(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        List<Question> allQuestions = questionRepository.findAll();
        
        java.util.Collections.shuffle(allQuestions);
        return allQuestions.stream().limit(20).toList();
    }

    
}