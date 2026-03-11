package com.cyberscale.backend.controllers;

import static org.hamcrest.Matchers.containsString;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.Recommendation;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ExamSessionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.repositories.RecommendationRepository;
import com.cyberscale.backend.repositories.UserRepository;
import com.cyberscale.backend.services.QuizService;
import com.cyberscale.backend.services.rabbitmq.RabbitMQProducer;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    
    @Autowired private UserRepository userRepository;
    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private ExamSessionRepository examSessionRepository;
    @Autowired private RecommendationRepository recommendationRepository;
    
    @MockitoBean 
    private QuizService quizService;

    @MockitoBean 
    private RabbitMQProducer rabbitMQProducer;

    private User testUser;

    @BeforeEach
    void setUp() {
        examSessionRepository.deleteAll();
        quizSessionRepository.deleteAll();
        userRepository.deleteAll();
        recommendationRepository.deleteAll();

        testUser = new User("ScoreTester", "score@test.com", "pass");
        testUser.setPoints(150);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testGetUser_ShouldReturnPoints() throws Exception {
        mockMvc.perform(get("/api/user/" + testUser.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("ScoreTester")))
                .andExpect(jsonPath("$.points", is(150)));
    }

    @Test
    void testGetUser_NotFound() throws Exception {
        mockMvc.perform(get("/api/user/999999")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetDashboardStats_Empty() throws Exception {
        mockMvc.perform(get("/api/user/" + testUser.getId() + "/dashboard")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageTheory", is(0.0)))
                .andExpect(jsonPath("$.totalQuizzes", is(0)))
                .andExpect(jsonPath("$.certifications", hasSize(0)));
    }

    @Test
    void testGetDashboardStats_WithData() throws Exception {
        createQuizSession(8.0, 8.0);
        createQuizSession(6.0, 6.0);

        ExamSession exam = new ExamSession();
        exam.setUser(testUser);
        exam.setExamRef("CyberWarrior");
        exam.setFinalScore(85);
        exam.setStatus("VALIDATED");
        exam.setStartTime(LocalDateTime.now());
        exam.setEndTime(LocalDateTime.now().plusHours(1));
        examSessionRepository.save(exam);

        Recommendation reco = new Recommendation();
        reco.setTitle("OWASP Top 10");
        reco.setType("PDF");
        reco.setUrl("https://owasp.org");
        recommendationRepository.save(reco);

        mockMvc.perform(get("/api/user/" + testUser.getId() + "/dashboard")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Quiz Stats
                .andExpect(jsonPath("$.totalQuizzes", is(2)))
                .andExpect(jsonPath("$.averageTheory", is(70.0))) 
                // Certifications
                .andExpect(jsonPath("$.certifications", hasSize(1)))
                .andExpect(jsonPath("$.certifications[0]", containsString("CyberWarrior")))
                // Ressources
                .andExpect(jsonPath("$.resources", hasSize(1)))
                .andExpect(jsonPath("$.resources[0].title", is("OWASP Top 10")));
    }

    private void createQuizSession(Double scoreTheo, Double scoreTech) {
        QuizSession q = new QuizSession();
        q.setUser(testUser);
        q.setFinalScoreTheory(scoreTheo);
        q.setFinalScoreTechnique(scoreTech);
        q.setCreatedAt(LocalDateTime.now());
        quizSessionRepository.save(q);
    }
}