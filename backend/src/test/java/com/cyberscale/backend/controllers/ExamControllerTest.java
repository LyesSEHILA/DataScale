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

    @Test
    void testFinishExam_CEH_ShouldUseCorrectThreshold() throws Exception {
        // 1. Session CEH
        ExamSession session = new ExamSession();
        session.setCandidateName("Hacker");
        session.setExamRef("CEH"); // On vise le if("CEH"...)
        session = examSessionRepository.save(session);

        // 2. Question (10 points)
        Question q = new Question();
        q.setText("Buffer Overflow");
        q.setPointsWeight(10);
        q.setExamRef("CEH");
        q = questionRepository.save(q);

        AnswerOption opt = new AnswerOption(null, "Yes", true);
        opt.setQuestion(q);
        opt = answerOptionRepository.save(opt);

        // 3. Réponse correcte (10/10 = 100%)
        String jsonAnswer = "{\"sessionId\": " + session.getId() + ", \"questionId\": " + q.getId() + ", \"optionId\": " + opt.getId() + "}";
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer))
                .andExpect(status().isOk());

        // 4. Finish
        MvcResult result = mockMvc.perform(post("/api/exam/finish/" + session.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 5. Vérif : 100% score / 0.75 seuil * 80 = 106 -> borné à 99%
        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        assertEquals(99, json.get("successProbability").asInt());
    }

    // --- TEST 6 : Couverture du cas Security+ (Correction du nom) ---
    @Test
    void testFinishExam_SecurityPlus_ShouldUseCorrectThreshold() throws Exception {
        ExamSession session = new ExamSession();
        session.setCandidateName("Admin");
        // ATTENTION : Le Service attend "Security+", pas "SEC_PLUS" pour le seuil !
        session.setExamRef("Security+"); 
        session = examSessionRepository.save(session);

        Question q = new Question();
        q.setText("Firewall");
        q.setPointsWeight(10);
        q = questionRepository.save(q);

        AnswerOption opt = new AnswerOption(null, "Allow", true);
        opt.setQuestion(q);
        opt = answerOptionRepository.save(opt);

        // Réponse
        String jsonAnswer = "{\"sessionId\": " + session.getId() + ", \"questionId\": " + q.getId() + ", \"optionId\": " + opt.getId() + "}";
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer));

        // Finish
        MvcResult result = mockMvc.perform(post("/api/exam/finish/" + session.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        
        // Vérification que le code est passé par la ligne officialThreshold = 0.83
        // 100% / 0.83 * 80 = ~96%
        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        // On accepte une petite marge d'erreur de calcul
        int prob = json.get("successProbability").asInt();
        assertEquals(true, prob >= 95 && prob <= 97, "Devrait être ~96%");
    }

    // --- TEST 7 : Couverture du cas Score Vide (0 questions) ---
    @Test
    void testFinishExam_EmptySession_ShouldHaveZeroProbability() throws Exception {
        // Session sans aucune question répondue (MaxScore = 0)
        ExamSession session = new ExamSession();
        session.setCandidateName("Newbie");
        session = examSessionRepository.save(session);

        MvcResult result = mockMvc.perform(post("/api/exam/finish/" + session.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        
        // On vérifie qu'on est passé dans le "else"
        assertEquals(0, json.get("finalScore").asInt());
        assertEquals(0, json.get("successProbability").asInt());
    }
}