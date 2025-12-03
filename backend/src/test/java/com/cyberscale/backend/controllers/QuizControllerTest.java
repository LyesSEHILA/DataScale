package com.cyberscale.backend.controllers;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.Recommendation;
import com.cyberscale.backend.models.UserAnswer;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.repositories.RecommendationRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print; // Pour voir les logs
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// On pointe vers un fichier inexistant pour être sûr à 100% qu'il ne charge pas data.sql
@TestPropertySource(properties = "spring.sql.init.data-locations=classpath:non-existent.sql")
public class QuizControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;
    @Autowired private RecommendationRepository recommendationRepository;

    // Nettoyage avant CHAQUE test pour garantir une base vide
    @BeforeEach
    void setup() {
        userAnswerRepository.deleteAllInBatch();
        answerOptionRepository.deleteAllInBatch();
        questionRepository.deleteAllInBatch();
        quizSessionRepository.deleteAllInBatch();
        recommendationRepository.deleteAllInBatch();
    }

    // --- TESTS F1 (Onboarding) ---
    @Test
    void testStartQuiz_ShouldReturn201_WhenRequestIsValid() throws Exception {
        OnboardingRequest request = new OnboardingRequest(25L, 5L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testStartQuiz_ShouldReturn400_WhenInvalid() throws Exception {
        OnboardingRequest request = new OnboardingRequest(0L, 5L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isBadRequest());
    }

    // --- TESTS F2 (Questions) ---
    @Test
    void testGetQuestions_ShouldReturnList_WhenSessionExists() throws Exception {
        // Arrange
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session.setSelfEvalTheory(5L);
        session.setSelfEvalTechnique(5L);
        session = quizSessionRepository.saveAndFlush(session);

        Question q1 = new Question();
        q1.setText("Test Question Theory Easy");
        q1.setCategorie(IQuestion.CategorieQuestion.THEORY);
        q1.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        questionRepository.saveAndFlush(q1);

        // Act & Assert
        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Test Question Theory Easy"));
    }

    @Test
    void testGetQuestions_ShouldReturnHardQuestions_WhenUserIsAdvanced() throws Exception {
        // Arrange
        QuizSession session = new QuizSession();
        session.setAge(30L);
        session.setSelfEvalTheory(9L);
        session.setSelfEvalTechnique(9L);
        session = quizSessionRepository.saveAndFlush(session);

        Question qHard = new Question();
        qHard.setText("Expert Question");
        qHard.setCategorie(IQuestion.CategorieQuestion.THEORY);
        qHard.setDifficulty(IQuestion.DifficultyQuestion.HARD);
        questionRepository.saveAndFlush(qHard);

        // Act & Assert
        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Expert Question"));
    }

    // --- TEST F2 (Submit Answer) ---
    @Test
    void testSubmitAnswer_ShouldReturn200_AndSaveAnswer() throws Exception {
        // Arrange
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session = quizSessionRepository.saveAndFlush(session);

        Question q1 = new Question();
        q1.setText("Question ?");
        q1.setCategorie(IQuestion.CategorieQuestion.THEORY);
        q1.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        q1 = questionRepository.saveAndFlush(q1);

        AnswerOption opt1 = new AnswerOption();
        opt1.setText("Reponse A");
        opt1.setIsCorrect(true);
        opt1.setQuestion(q1);
        opt1 = answerOptionRepository.saveAndFlush(opt1);

        UserAnswerRequest request = new UserAnswerRequest(session.getId(), q1.getId(), opt1.getId());
        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/quiz/answer")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk());

        assertEquals(1, userAnswerRepository.count());
    }

    // --- TESTS F3/F4 (Résultats) ---
    @Test
    void testGetResults_ShouldReturnScoresAndRecos() throws Exception {
        // Arrange
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session = quizSessionRepository.saveAndFlush(session);

        Question qTheory = new Question();
        qTheory.setText("Théorie?");
        qTheory.setCategorie(IQuestion.CategorieQuestion.THEORY);
        qTheory.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        qTheory = questionRepository.saveAndFlush(qTheory);

        AnswerOption optTheoryCorrect = new AnswerOption(null, "Vrai", true);
        optTheoryCorrect.setQuestion(qTheory);
        optTheoryCorrect = answerOptionRepository.saveAndFlush(optTheoryCorrect);

        // On ajoute une fausse réponse utilisateur
        UserAnswer ans1 = new UserAnswer(session, qTheory, optTheoryCorrect);
        userAnswerRepository.saveAndFlush(ans1);

        Recommendation reco = new Recommendation();
        reco.setTitle("Livre Test");
        reco.setTargetProfile("LOW_TECH"); // Tech score sera 0
        recommendationRepository.saveAndFlush(reco);

        // Act & Assert
        mockMvc.perform(get("/api/quiz/results")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Pour voir le JSON si ça échoue
                .andExpect(status().isOk())
                // On utilise .value(10.0) pour vérifier le double
                .andExpect(jsonPath("$.scoreTheory").value(10.0))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations", hasSize(1)));
    }
}