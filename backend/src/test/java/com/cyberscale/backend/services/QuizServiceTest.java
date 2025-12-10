package com.cyberscale.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cyberscale.backend.dto.HistoryDTO;
import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.dto.ResultsResponse;
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.*;
import com.cyberscale.backend.repositories.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizSessionRepository quizSessionRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private AnswerOptionRepository answerOptionRepository;
    @Mock private UserAnswerRepository userAnswerRepository;
    @Mock private RecommendationRepository recommendationRepository;
    @Mock private UserRepository userRepository;
    @Mock private ExamSessionRepository examSessionRepository;
    @Mock private QuestionGenerator questionGenerator;

    @InjectMocks private QuizService quizService;

    private QuizSession quizSession;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("TestUser", "test@test.com", "pass");
        user.setId(1L);
        quizSession = new QuizSession();
        quizSession.setId(10L);
        quizSession.setUser(user);
    }

    // --- TEST 1 : Création de Session (Avec et Sans User) ---
    // --- TEST 1 : Création de Session (Avec et Sans User) ---
    @Test
    void createSession_ShouldHandleUserPresence() {
        // Cas avec User
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        quizService.createSession(new OnboardingRequest(25L, 5L, 5L, 1L)); 
        
        verify(quizSessionRepository, times(1)).save(argThat(s -> s.getUser() != null));

        quizService.createSession(new OnboardingRequest(25L, 5L, 5L, null)); 
        
        verify(quizSessionRepository, times(2)).save(any());
    }

    // --- TEST 2 : Calcul des Résultats (Branches Theory/Tech) ---
    @Test
    void calculateAndGetResults_ShouldCategorizeCorrectly() {
        // Setup Questions
        Question qTheory = new Question(); qTheory.setCategorie(IQuestion.CategorieQuestion.THEORY);
        Question qTech = new Question(); qTech.setCategorie(IQuestion.CategorieQuestion.TECHNIQUE);
        
        AnswerOption correct = new AnswerOption(); correct.setIsCorrect(true);
        AnswerOption wrong = new AnswerOption(); wrong.setIsCorrect(false);

        // Setup Réponses Utilisateur
        UserAnswer a1 = new UserAnswer(quizSession, qTheory, correct);
        UserAnswer a2 = new UserAnswer(quizSession, qTech, wrong);
        
        when(quizSessionRepository.findById(10L)).thenReturn(Optional.of(quizSession));
        when(userAnswerRepository.findBySessionId(10L)).thenReturn(List.of(a1, a2));
        
        ResultsResponse res = quizService.calculateAndGetResults(10L);
        
        assertEquals(10.0, res.scoreTheory()); // 1/1 correct = 100% -> 10/10
        assertEquals(0.0, res.scoreTechnique()); // 0/1 correct = 0% -> 0/10
        verify(quizSessionRepository).save(any());
    }

    @Test
    void calculateAndGetResults_EmptyAnswers() {
        when(quizSessionRepository.findById(10L)).thenReturn(Optional.of(quizSession));
        when(userAnswerRepository.findBySessionId(10L)).thenReturn(Collections.emptyList());

        ResultsResponse res = quizService.calculateAndGetResults(10L);
        assertEquals(0.0, res.scoreTheory());
    }

    // --- TEST 3 : Historique Utilisateur (Gros morceau pour le coverage) ---
    @Test
    void getUserHistory_ShouldMapAllTypesAndHandleNulls() {
        // 1. Setup Quiz (un fini, un avec scores nulls)
        QuizSession q1 = new QuizSession(); 
        q1.setId(1L); q1.setCreatedAt(LocalDateTime.now());
        q1.setFinalScoreTheory(8.0); q1.setFinalScoreTechnique(6.0);

        QuizSession q2 = new QuizSession(); // Scores nulls par défaut
        q2.setId(2L); q2.setCreatedAt(LocalDateTime.now().minusDays(1));

        // 2. Setup Examens
        ExamSession e1 = new ExamSession(); // CEH
        e1.setId(3L); e1.setExamRef("CEH"); e1.setStartTime(LocalDateTime.now());
        // CORRECTION ICI : 60 au lieu de 70 pour être sûr d'avoir "Échoué" (< 70%)
        e1.setFinalScore(60); 
        e1.setMaxPossibleScore(100); 

        ExamSession e2 = new ExamSession(); // Security+
        e2.setId(4L); e2.setExamRef("SEC_PLUS"); e2.setStartTime(LocalDateTime.now());
        e2.setFinalScore(90); e2.setMaxPossibleScore(100); // 90% -> Pass

        ExamSession e3 = new ExamSession(); // CISSP
        e3.setId(5L); e3.setExamRef("CISSP"); e3.setStartTime(LocalDateTime.now());
        
        ExamSession e4 = new ExamSession(); // Autre (Default)
        e4.setId(6L); e4.setExamRef("UNKNOWN"); e4.setStartTime(LocalDateTime.now());

        // Mocking
        when(quizSessionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(q1, q2));
        when(examSessionRepository.findByUserId(1L)).thenReturn(List.of(e1, e2, e3, e4));

        // Execution
        List<HistoryDTO> history = quizService.getUserHistory(1L);

        // Assertions
        assertEquals(6, history.size()); 
        
        assertTrue(history.stream().anyMatch(h -> h.title().equals("Évaluation Initiale")));
        assertTrue(history.stream().anyMatch(h -> h.title().equals("CEH v12 Simulator")));
        assertTrue(history.stream().anyMatch(h -> h.title().equals("CompTIA Security+")));
        assertTrue(history.stream().anyMatch(h -> h.title().equals("CISSP Manager")));
        
        // Cette assertion passera maintenant car 60% < 70%
        assertTrue(history.stream().anyMatch(h -> h.status().equals("Échoué ❌") && h.title().equals("CEH v12 Simulator")));
    }
    
    @Test
    void saveUserAnswer_ShouldWork() {
        // Test simple pour couvrir saveUserAnswer
        when(quizSessionRepository.findById(any())).thenReturn(Optional.of(quizSession));
        when(questionRepository.findById(any())).thenReturn(Optional.of(new Question()));
        when(answerOptionRepository.findById(any())).thenReturn(Optional.of(new AnswerOption()));
        
        quizService.saveUserAnswer(new UserAnswerRequest(1L, 1L, 1L));
        
        verify(userAnswerRepository).save(any());
    }
    
    @Test
    void getQuestionsForSession_ShouldWork() {
        when(quizSessionRepository.findById(1L)).thenReturn(Optional.of(quizSession));
        quizService.getQuestionsForSession(1L);
        verify(questionGenerator).generate(quizSession);
    }
}