package com.cyberscale.backend.controllers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
    void testGetUserHistory_ShouldReturnMixedAndSortedHistory() throws Exception {
        // 1. Créer une session QUIZ (Ancienne)
        QuizSession quiz = new QuizSession();
        quiz.setUser(testUser);
        quiz.setFinalScoreTheory(8.0);
        quiz.setFinalScoreTechnique(6.0);
        quiz.setCreatedAt(LocalDateTime.now().minusDays(2)); // Il y a 2 jours
        quizSessionRepository.save(quiz);

        // 2. Créer une session EXAMEN (Récente - Échouée)
        ExamSession examFail = new ExamSession();
        examFail.setUser(testUser);
        examFail.setCandidateName("HistoryTester");
        examFail.setFinalScore(10);
        examFail.setMaxPossibleScore(100); // 10% -> Échec
        examFail.setStartTime(LocalDateTime.now().minusHours(1));
        examSessionRepository.save(examFail);

        // 3. Créer une session EXAMEN (Très Récente - Réussie)
        ExamSession examSuccess = new ExamSession();
        examSuccess.setUser(testUser);
        examSuccess.setCandidateName("HistoryTester");
        examSuccess.setFinalScore(80);
        examSuccess.setMaxPossibleScore(100); // 80% -> Succès
        examSuccess.setStartTime(LocalDateTime.now()); // Maintenant
        examSessionRepository.save(examSuccess);

        // 4. Appel API
        mockMvc.perform(get("/api/user/" + testUser.getId() + "/history")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // 3 éléments au total
                // Vérifier le tri (le plus récent en premier = examSuccess)
                .andExpect(jsonPath("$[0].type", is("EXAMEN")))
                .andExpect(jsonPath("$[0].status", is("Validé ✅")))
                // Le deuxième (examFail)
                .andExpect(jsonPath("$[1].status", is("Échoué ❌")))
                // Le dernier (le quiz)
                .andExpect(jsonPath("$[2].type", is("QUIZ")));
    }
    
    @Test
    void testGetUserHistory_Empty() throws Exception {
        mockMvc.perform(get("/api/user/" + testUser.getId() + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}