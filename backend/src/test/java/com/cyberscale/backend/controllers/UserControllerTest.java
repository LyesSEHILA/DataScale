package com.cyberscale.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ExamSessionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=")
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private ExamSessionRepository examSessionRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        quizSessionRepository.deleteAll();
        examSessionRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("HistoryTester", "hist@test.com", "pass");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testGetUserHistory_ShouldTranslateTitlesCorrectly() throws Exception {
        // 1. Quiz (Ancien)
        QuizSession quiz = new QuizSession();
        quiz.setUser(testUser);
        quiz.setCreatedAt(LocalDateTime.now().minusDays(5));
        quizSessionRepository.save(quiz);

        // 2. Exam Security+ (Récent)
        ExamSession examSec = new ExamSession();
        examSec.setUser(testUser);
        examSec.setExamRef("SEC_PLUS"); // Code technique
        examSec.setFinalScore(100);
        examSec.setMaxPossibleScore(100);
        examSec.setStartTime(LocalDateTime.now().minusDays(1));
        examSessionRepository.save(examSec);

        // 3. Exam CEH (Très récent)
        ExamSession examCeh = new ExamSession();
        examCeh.setUser(testUser);
        examCeh.setExamRef("CEH");
        examCeh.setFinalScore(20);
        examCeh.setMaxPossibleScore(100);
        examCeh.setStartTime(LocalDateTime.now());
        examSessionRepository.save(examCeh);

        // 4. Validation
        mockMvc.perform(get("/api/user/" + testUser.getId() + "/history")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                // Vérifier l'ordre (CEH en premier) et le TITRE traduit
                .andExpect(jsonPath("$[0].title", is("CEH v12 Simulator")))
                .andExpect(jsonPath("$[0].status", is("Échoué ❌")))
                // Vérifier Security+
                .andExpect(jsonPath("$[1].title", is("CompTIA Security+")))
                .andExpect(jsonPath("$[1].status", is("Validé ✅")))
                // Vérifier Quiz
                .andExpect(jsonPath("$[2].type", is("QUIZ")));
    }
}