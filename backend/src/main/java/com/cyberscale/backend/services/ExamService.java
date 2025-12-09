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

@Service
public class ExamService {

    @Autowired private ExamSessionRepository examSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;
    @Autowired private UserRepository userRepository;


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
     * Soumettre une réponse avec VALIDATION TEMPORELLE
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
        // -----------------------------

        if (LocalDateTime.now().isBefore(session.getEndTime())) {
            session.setEndTime(LocalDateTime.now());
        }

        return examSessionRepository.save(session);
    }

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

    public Map<String, Object> getExamStatus(Long sessionId) {
    ExamSession session = examSessionRepository.findById(sessionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

    long secondsLeft = java.time.Duration.between(LocalDateTime.now(), session.getEndTime()).getSeconds();
    if (secondsLeft < 0) secondsLeft = 0;

    // ON AJOUTE CECI :
    long answeredCount = userAnswerRepository.countByExamSessionId(sessionId);

    return Map.of(
        "secondsLeft", secondsLeft,
        "isFinished", session.getFinalScore() != null,
        "examRef", session.getExamRef() != null ? session.getExamRef() : "EXAM",
        "currentIndex", answeredCount // On renvoie l'index où reprendre (ex: 3 réponses = index 3, donc 4ème question)
    );
}
    
}