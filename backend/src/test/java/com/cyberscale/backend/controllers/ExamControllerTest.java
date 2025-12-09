package com.cyberscale.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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

    // --- TEST 1 : Démarrage de l'examen avec Ref ---
    @Test
    void testStartExam_ShouldReturnSessionWithRef() throws Exception {
        mockMvc.perform(post("/api/exam/start")
                .param("candidateName", "John Doe")
                .param("examRef", "SEC_PLUS") // On teste le paramètre
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.examRef").value("SEC_PLUS")) // Vérif
                .andExpect(jsonPath("$.candidateName").value("John Doe"));
    }

    // --- TEST 2 : Vérification du Score et Probabilité ---
    @Test
    void testFinishExam_ShouldCalculateScoreAndProbability() throws Exception {
        // 1. Créer une Session Security+
        ExamSession session = new ExamSession();
        session.setCandidateName("Tester");
        session.setExamRef("SEC_PLUS");
        session = examSessionRepository.save(session);

        // 2. Créer une question (5 points)
        Question q1 = new Question();
        q1.setText("Q1");
        q1.setPointsWeight(5);
        q1.setExamRef("SEC_PLUS");
        q1 = questionRepository.save(q1);

        AnswerOption optCorrect = new AnswerOption(null, "Correct", true);
        optCorrect.setQuestion(q1);
        optCorrect = answerOptionRepository.save(optCorrect);

        // 3. Répondre juste
        String jsonAnswer = "{\"sessionId\": " + session.getId() + ", \"questionId\": " + q1.getId() + ", \"optionId\": " + optCorrect.getId() + "}";
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer))
                .andExpect(status().isOk());

        // 4. Terminer
        MvcResult result = mockMvc.perform(post("/api/exam/finish/" + session.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        
        // 5. Vérifications
        assertEquals(5, json.get("finalScore").asInt());
        // Avec 100% de réussite (5/5), la probabilité devrait être élevée (>80%)
        assertNotNull(json.get("successProbability"));
        int prob = json.get("successProbability").asInt();
        assertEquals(true, prob > 50, "La probabilité doit être positive pour un score parfait");
    }

    // --- TEST 3 : Filtrage des questions ---
    @Test
    void testGetQuestions_ShouldFilterByExamRef() throws Exception {
        // Session CEH
        ExamSession session = new ExamSession();
        session.setExamRef("CEH"); 
        session = examSessionRepository.save(session);

        // Question CEH
        Question qCEH = new Question(); 
        qCEH.setText("CEH Question"); 
        qCEH.setExamRef("CEH");
        questionRepository.save(qCEH);

        // Question Autre (ne doit pas apparaître)
        Question qOther = new Question(); 
        qOther.setText("Other"); 
        qOther.setExamRef("CISSP");
        questionRepository.save(qOther);

        mockMvc.perform(get("/api/exam/" + session.getId() + "/questions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // On ne doit avoir que la question CEH
                .andExpect(jsonPath("$[0].text").value("CEH Question"));
    }
    
    // --- TEST 4 : Status ---
    @Test
    void testGetStatus_ShouldReturnTimeAndIndex() throws Exception {
        ExamSession session = new ExamSession();
        session.setEndTime(LocalDateTime.now().plusMinutes(10));
        session = examSessionRepository.save(session);
        
        mockMvc.perform(get("/api/exam/" + session.getId() + "/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.secondsLeft").exists())
            .andExpect(jsonPath("$.currentIndex").value(0));
    }
}