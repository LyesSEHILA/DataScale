package com.cyberscale.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "spring.sql.init.mode=never")
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository; // <--- NOUVEAU : Pour nettoyer les réponses

    // --- TESTS F1 (Onboarding) ---

    @Test
    void testStartQuiz_ShouldReturn201_WhenRequestIsValid() throws Exception {
        OnboardingRequest request = new OnboardingRequest(25L, 5L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void testStartQuiz_ShouldReturn400_WhenAgeIsInvalid() throws Exception {
        OnboardingRequest request = new OnboardingRequest(0L, 5L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testStartQuiz_ShouldReturn400_WhenTheoryEvalIsInvalid() throws Exception {
        OnboardingRequest request = new OnboardingRequest(25L, 11L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    // --- TESTS F2 (Questions) ---

    @Test
    void testGetQuestions_ShouldReturnList_WhenSessionExists() throws Exception {
        // 1. NETTOYAGE COMPLET (Ordre important : Enfants d'abord, Parents ensuite)
        answerOptionRepository.deleteAll(); // On supprime les réponses d'abord !
        questionRepository.deleteAll();     // Ensuite on peut supprimer les questions
        quizSessionRepository.deleteAll();

        // 2. Préparation
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session.setSelfEvalTheory(5L);
        session.setSelfEvalTechnique(5L);
        session = quizSessionRepository.save(session);

        Question q1 = new Question();
        q1.setText("Test Question Theory Easy");
        q1.setCategorie(Question.categorieQuestion.THEORY);
        q1.setDifficulty(Question.difficultyQuestion.EASY);
        questionRepository.save(q1);

        // 3. Action & Vérification
        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Test Question Theory Easy"));
    }

    @Test
    void testGetQuestions_ShouldReturn404_WhenSessionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", "9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetQuestions_ShouldReturnHardQuestions_WhenUserIsAdvanced() throws Exception {
        // 1. Préparation : On crée un utilisateur EXPERT (Scores > 5)
        QuizSession session = new QuizSession();
        session.setAge(30L);
        session.setSelfEvalTheory(9L);    // Expert Théorie
        session.setSelfEvalTechnique(9L); // Expert Technique
        session = quizSessionRepository.save(session);

        // On insère une question DIFFICILE pour vérifier qu'on la récupère bien
        Question qHard = new Question();
        qHard.setText("Expert Question");
        qHard.setCategorie(Question.categorieQuestion.THEORY);
        qHard.setDifficulty(Question.difficultyQuestion.HARD);
        questionRepository.save(qHard);

        // 2. Action
        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                // 3. Vérification
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Expert Question"));
    }
    
}