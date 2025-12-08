package com.cyberscale.backend.controllers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals; // Import pour GET
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource; // Import indispensable
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.ExamSessionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=")
public class ExamControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ExamSessionRepository examSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;

    @BeforeEach
    void cleanUp() {
        userAnswerRepository.deleteAll();
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        examSessionRepository.deleteAll();
    }

    // --- TEST 1 : Démarrage de l'examen ---
    @Test 
    void testStartExam_ShouldReturnSession() throws Exception {
        mockMvc.perform(post("/api/exam/start")
                .param("candidateName", "John Doe")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.candidateName").value("John Doe"));
    }

    // --- TEST 2 : Vérification du Score Pondéré ---
    @Test
    void testFinishExam_ShouldCalculateWeightedScore() throws Exception {
        ExamSession session = new ExamSession();
        session.setCandidateName("Tester");
        session = examSessionRepository.save(session);

        Question qHard = new Question();
        qHard.setText("Question Difficile");
        qHard.setCategorie(Question.CategorieQuestion.THEORY);
        qHard.setDifficulty(Question.DifficultyQuestion.HARD);
        qHard.setPointsWeight(5);
        qHard = questionRepository.save(qHard);

        AnswerOption optCorrect = new AnswerOption(null, "Bonne réponse", true);
        optCorrect.setQuestion(qHard);
        optCorrect = answerOptionRepository.save(optCorrect);

        String jsonAnswer = "{\"sessionId\": " + session.getId() + ", \"questionId\": " + qHard.getId() + ", \"optionId\": " + optCorrect.getId() + "}";
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/exam/finish/" + session.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        
        assertEquals(5, json.get("finalScore").asInt(), "Le score doit être de 5 pour une question HARD");
    }

    // --- TEST 3 : Blocage si temps écoulé ---
    @Test
    void testSubmitAnswer_ShouldFail_WhenTimeIsOver() throws Exception {
        ExamSession expiredSession = new ExamSession();
        expiredSession.setCandidateName("Late Candidate");
        expiredSession.setEndTime(LocalDateTime.now().minusMinutes(10)); 
        expiredSession = examSessionRepository.save(expiredSession);

        Question q = new Question(); 
        q.setText("Q");
        q = questionRepository.save(q);
        
        AnswerOption opt = new AnswerOption(null, "A", true);
        opt.setQuestion(q);
        opt = answerOptionRepository.save(opt);

        String jsonAnswer = "{\"sessionId\": " + expiredSession.getId() + ", \"questionId\": " + q.getId() + ", \"optionId\": " + opt.getId() + "}";
        
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer))
                .andExpect(status().isForbidden());
    }

    // --- NOUVEAU TEST 4 : Récupération des questions ---
    @Test
    void testGetQuestions_ShouldReturnList() throws Exception {
        ExamSession session = new ExamSession();
        session.setCandidateName("Reader");
        session = examSessionRepository.save(session);

        Question q1 = new Question(); q1.setText("Q1"); questionRepository.save(q1);
        Question q2 = new Question(); q2.setText("Q2"); questionRepository.save(q2);

        mockMvc.perform(get("/api/exam/" + session.getId() + "/questions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); 
    }

    @Test
    void testFinishExam_ShouldReturn404_WhenSessionNotFound() throws Exception {
        mockMvc.perform(post("/api/exam/finish/999999") // ID inexistant
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAnswer_ShouldReturn404_WhenSessionNotFound() throws Exception {
        String jsonAnswer = "{\"sessionId\": 99999, \"questionId\": 1, \"optionId\": 1}";
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetQuestions_ShouldReturn404_WhenSessionNotFound() throws Exception {
        mockMvc.perform(get("/api/exam/99999/questions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}