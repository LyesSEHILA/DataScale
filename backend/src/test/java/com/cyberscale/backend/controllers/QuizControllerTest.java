package com.cyberscale.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.Recommendation;
import com.cyberscale.backend.models.UserAnswer;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.repositories.RecommendationRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;
import com.cyberscale.backend.services.rabbitmq.RabbitMQProducer;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=classpath:non-existent.sql")
class QuizControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;
    @Autowired private RecommendationRepository recommendationRepository;
    @Autowired private com.cyberscale.backend.repositories.UserRepository userRepository; 

    @MockitoBean 
    private RabbitMQProducer rabbitMQProducer;

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
        OnboardingRequest request = new OnboardingRequest(25L, 5L, 7L, null);
        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void testStartQuiz_ShouldReturn400_WhenInvalid() throws Exception {
        OnboardingRequest request = new OnboardingRequest(0L, 5L, 7L, null);
        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isBadRequest());
    }

    // --- TESTS F2 (Questions) ---
    @Test
    void testGetQuestions_ShouldReturnList_WhenSessionExists() throws Exception {
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

        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Test Question Theory Easy"));
    }

    @Test
    void testGetQuestions_ShouldReturnHardQuestions_WhenUserIsAdvanced() throws Exception {
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

        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Expert Question"));
    }

    // --- TEST F2 (Submit Answer) ---
    @Test
    void testSubmitAnswer_ShouldReturn200_AndSaveAnswer() throws Exception {
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

        mockMvc.perform(post("/api/quiz/answer")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk());

        assertEquals(1, userAnswerRepository.count());
    }

    // --- TESTS F3/F4 (Résultats) ---
    @Test
    void testGetResults_ShouldReturnScoresAndRecos() throws Exception {
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

        Question qTech = new Question();
        qTech.setText("Tech?");
        qTech.setCategorie(IQuestion.CategorieQuestion.TECHNIQUE);
        qTech.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        qTech = questionRepository.saveAndFlush(qTech);

        AnswerOption optTechWrong = new AnswerOption(null, "Faux", false);
        optTechWrong.setQuestion(qTech);
        optTechWrong = answerOptionRepository.saveAndFlush(optTechWrong);

        UserAnswer ans1 = new UserAnswer(session, qTheory, optTheoryCorrect);
        userAnswerRepository.saveAndFlush(ans1);
        
        UserAnswer ans2 = new UserAnswer(session, qTech, optTechWrong);
        userAnswerRepository.saveAndFlush(ans2);

        Recommendation reco = new Recommendation();
        reco.setTitle("Livre Test");
        reco.setTargetProfile("LOW_TECH"); 
        recommendationRepository.saveAndFlush(reco);

        mockMvc.perform(get("/api/quiz/results")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scoreTheory").value(10.0)) 
                .andExpect(jsonPath("$.scoreTechnique").value(0.0))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations", hasSize(1)));
    }

    @Test
    void testGetResults_ShouldReturnZeros_WhenNoAnswers() throws Exception {
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session = quizSessionRepository.saveAndFlush(session);

        mockMvc.perform(get("/api/quiz/results")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scoreTheory").value(0.0))
                .andExpect(jsonPath("$.scoreTechnique").value(0.0));
    }

    // --- NOUVEAUX TESTS DE COUVERTURE (F2 - Generator) ---
    @Test
    void testGetQuestions_ShouldHandleZeroScores() throws Exception {
        QuizSession session = new QuizSession();
        session.setAge(20L);
        session.setSelfEvalTheory(0L);
        session.setSelfEvalTechnique(0L);
        session = quizSessionRepository.saveAndFlush(session);

        for (int i = 0; i < 5; i++) {
            Question q = new Question();
            q.setText("Q" + i);
            q.setCategorie(IQuestion.CategorieQuestion.THEORY);
            q.setDifficulty(IQuestion.DifficultyQuestion.EASY);
            questionRepository.saveAndFlush(q);
        }
        for (int i = 0; i < 5; i++) {
            Question q = new Question();
            q.setText("Q" + (i+5));
            q.setCategorie(IQuestion.CategorieQuestion.TECHNIQUE);
            q.setDifficulty(IQuestion.DifficultyQuestion.EASY);
            questionRepository.saveAndFlush(q);
        }

        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    void testGetQuestions_ShouldFilterTooEasyQuestions() throws Exception {
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session.setSelfEvalTheory(5L);
        session.setSelfEvalTechnique(5L);
        session = quizSessionRepository.saveAndFlush(session);

        // Question A : Trop facile
        Question qEasy = new Question();
        qEasy.setText("Trop Facile");
        qEasy.setCategorie(IQuestion.CategorieQuestion.THEORY);
        qEasy.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        qEasy = questionRepository.saveAndFlush(qEasy);

        AnswerOption optCorrect = new AnswerOption(null, "Vrai", true);
        optCorrect.setQuestion(qEasy);
        optCorrect = answerOptionRepository.saveAndFlush(optCorrect);

        // 5 réussites pour déclencher le filtre
        for (int i = 0; i < 5; i++) {
            UserAnswer success = new UserAnswer(session, qEasy, optCorrect);
            userAnswerRepository.saveAndFlush(success);
        }

        // Question B : Normale
        Question qNormal = new Question();
        qNormal.setText("Normale");
        qNormal.setCategorie(IQuestion.CategorieQuestion.THEORY);
        qNormal.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        qNormal = questionRepository.saveAndFlush(qNormal);

        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Normale")) 
                .andExpect(jsonPath("$", hasSize(1))); 
    }

    @Test
    void testStartQuiz_WithUser_ShouldLinkUser() throws Exception {
        com.cyberscale.backend.models.User user = new com.cyberscale.backend.models.User("testF6", "f6@test.com", "pass");
        user = userRepository.saveAndFlush(user);

        OnboardingRequest request = new OnboardingRequest(25L, 5L, 5L, user.getId());
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.user.id").value(user.getId()));
    }
}