package com.cyberscale.backend.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.Recommendation;
import com.cyberscale.backend.models.UserAnswer;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.repositories.RecommendationRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// On force Spring à ne charger AUCUN fichier de données SQL pour les tests
@TestPropertySource(properties = "spring.sql.init.data-locations=") 
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
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private RecommendationRepository recommendationRepository; // <--- NOUVEAU pour F4

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
        // Nettoyage préventif
        userAnswerRepository.deleteAll();
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        quizSessionRepository.deleteAll();

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
        QuizSession session = new QuizSession();
        session.setAge(30L);
        session.setSelfEvalTheory(9L);
        session.setSelfEvalTechnique(9L);
        session = quizSessionRepository.save(session);

        Question qHard = new Question();
        qHard.setText("Expert Question");
        qHard.setCategorie(Question.categorieQuestion.THEORY);
        qHard.setDifficulty(Question.difficultyQuestion.HARD);
        questionRepository.save(qHard);

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
        session = quizSessionRepository.save(session);

        Question q1 = new Question();
        q1.setText("Question ?");
        q1.setCategorie(Question.categorieQuestion.THEORY);
        q1.setDifficulty(Question.difficultyQuestion.EASY);
        q1 = questionRepository.save(q1);

        AnswerOption opt1 = new AnswerOption();
        opt1.setText("Reponse A");
        opt1.setIsCorrect(true);
        opt1.setQuestion(q1); 
        opt1 = answerOptionRepository.save(opt1);

        UserAnswerRequest request = new UserAnswerRequest(session.getId(), q1.getId(), opt1.getId());
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/quiz/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk()); 

        assertEquals(1, userAnswerRepository.count()); 
    }

    // --- NOUVEAUX TESTS F3/F4 (Résultats) ---

    @Test
    void testGetResults_ShouldReturnScoresAndRecos() throws Exception {
        // 1. Préparation : Session
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session = quizSessionRepository.save(session);

        // 2. Préparation : Question THÉORIE + Bonne Réponse
        Question qTheory = new Question();
        qTheory.setText("Théorie?");
        qTheory.setCategorie(Question.categorieQuestion.THEORY);
        qTheory.setDifficulty(Question.difficultyQuestion.EASY);
        qTheory = questionRepository.save(qTheory);

        AnswerOption optTheoryCorrect = new AnswerOption(null, "Vrai", true);
        optTheoryCorrect.setQuestion(qTheory);
        optTheoryCorrect = answerOptionRepository.save(optTheoryCorrect);

        // 3. Préparation : Question TECHNIQUE + Mauvaise Réponse
        Question qTech = new Question();
        qTech.setText("Tech?");
        qTech.setCategorie(Question.categorieQuestion.TECHNIQUE);
        qTech.setDifficulty(Question.difficultyQuestion.EASY);
        qTech = questionRepository.save(qTech);

        AnswerOption optTechWrong = new AnswerOption(null, "Faux", false);
        optTechWrong.setQuestion(qTech);
        optTechWrong = answerOptionRepository.save(optTechWrong);

        // 4. Enregistrement des réponses utilisateur
        UserAnswer ans1 = new UserAnswer(session, qTheory, optTheoryCorrect);
        userAnswerRepository.save(ans1);
        
        UserAnswer ans2 = new UserAnswer(session, qTech, optTechWrong);
        userAnswerRepository.save(ans2);

        // 5. Ajouter une recommandation en base pour vérifier qu'elle remonte
        Recommendation reco = new Recommendation();
        reco.setTitle("Livre Test");
        reco.setTargetProfile("LOW_TECH"); // Car Tech score sera 0
        recommendationRepository.save(reco);

        // 6. Exécution : GET /results
        mockMvc.perform(get("/api/quiz/results")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Théorie : 1/1 juste -> 10.0
                .andExpect(jsonPath("$.scoreTheory").value(10.0)) 
                // Technique : 0/1 juste -> 0.0
                .andExpect(jsonPath("$.scoreTechnique").value(0.0))
                // Vérifie qu'on a bien des recommandations
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations", hasSize(1)));
    }

    @Test
    void testGetResults_ShouldReturnZeros_WhenNoAnswers() throws Exception {
        QuizSession session = new QuizSession();
        session.setAge(25L);
        session = quizSessionRepository.save(session);

        mockMvc.perform(get("/api/quiz/results")
                .param("sessionId", session.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scoreTheory").value(0.0))
                .andExpect(jsonPath("$.scoreTechnique").value(0.0));
    }
}