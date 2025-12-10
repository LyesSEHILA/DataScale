package com.cyberscale.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.models.*;
import com.cyberscale.backend.repositories.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock private ExamSessionRepository examSessionRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private AnswerOptionRepository answerOptionRepository;
    @Mock private UserAnswerRepository userAnswerRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ExamService examService;

    private ExamSession session;

    @BeforeEach
    void setUp() {
        session = new ExamSession();
        session.setId(1L);
        session.setEndTime(LocalDateTime.now().plusMinutes(30)); // Session valide
    }

    // --- TEST 1 : Soumission hors délai ---
    @Test
    void submitExamAnswer_ShouldThrowIfTimeExpired() {
        session.setEndTime(LocalDateTime.now().minusMinutes(1)); // Session expirée
        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThrows(ResponseStatusException.class, () -> 
            examService.submitExamAnswer(1L, 1L, 1L)
        );
    }

    // --- TEST 2 : Start Exam (Avec et Sans User) ---
    @Test
    void startExam_ShouldHandleUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        examService.startExam("Candidat", 1L, "CEH");
        verify(examSessionRepository).save(argThat(s -> s.getUser() != null));

        examService.startExam("Candidat", null, "CEH");
        verify(examSessionRepository, times(2)).save(any());
    }

    // --- TEST 3 : Calcul de Probabilité (Branches if/else if) ---
    @Test
    void finishExam_ShouldCalculateProbabilitiesCorrectly() {
        // Setup Questions/Réponses
        Question q1 = new Question(); q1.setPointsWeight(10);
        AnswerOption a1 = new AnswerOption(); a1.setIsCorrect(true);
        UserAnswer ua1 = new UserAnswer(); ua1.setExamSession(session); ua1.setQuestion(q1); ua1.setSelectedOption(a1);

        when(userAnswerRepository.findAll()).thenReturn(List.of(ua1));
        when(examSessionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Cas 1 : Security+ (Seuil 0.83)
        session.setExamRef("Security+");
        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        ExamSession resSec = examService.finishExam(1L);
        assertTrue(resSec.getSuccessProbability() > 0); // Vérifie qu'on passe dans le calcul

        // Cas 2 : CEH (Seuil 0.75)
        session.setExamRef("CEH");
        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        ExamSession resCeh = examService.finishExam(1L);
        assertTrue(resCeh.getSuccessProbability() > 0);
        
        // Cas 3 : Autre (Seuil 0.70)
        session.setExamRef("CISSP");
        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        examService.finishExam(1L);
    }
    
    @Test
    void getExamQuestions_ShouldFilterByRef() {
        session.setExamRef("CEH");
        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        
        examService.getExamQuestions(1L);
        verify(questionRepository).findByExamRef("CEH");
        
        session.setExamRef(null);
        examService.getExamQuestions(1L);
        verify(questionRepository).findAll();
    }
    
    @Test
    void getExamStatus_ShouldReturnDetails() {
        when(examSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userAnswerRepository.countByExamSessionId(1L)).thenReturn(5L);
        
        var status = examService.getExamStatus(1L);
        assertEquals(5L, status.get("currentIndex"));
    }
}