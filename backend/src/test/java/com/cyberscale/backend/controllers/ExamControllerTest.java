package com.cyberscale.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécurité pour les tests
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=") // On ignore data.sql
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
    void testStartExam_ShouldReturnSession() throws Exception {
    mockMvc.perform(post("/api/exam/start")
            .param("candidateName", "John Doe")
            // Retirez .contentType(...) car on envoie un simple paramètre de formulaire/URL, pas un JSON Body
            .accept(MediaType.APPLICATION_JSON)) // On accepte du JSON en réponse
            .andDo(print()) // <--- AFFICHERA L'ERREUR DANS LA CONSOLE SI ÇA ECHOUE ENCORE
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.candidateName").value("John Doe"));
}

    // --- TEST 2 : Vérification du Score Pondéré ---
    @Test
    void testFinishExam_ShouldCalculateWeightedScore() throws Exception {
        // 1. Créer une Session
        ExamSession session = new ExamSession();
        session.setCandidateName("Tester");
        session = examSessionRepository.save(session);

        // 2. Créer une question DIFFICILE (Poids = 5)
        Question qHard = new Question();
        qHard.setText("Question Difficile");
        qHard.setCategorie(Question.CategorieQuestion.THEORY);
        qHard.setDifficulty(Question.DifficultyQuestion.HARD);
        qHard.setPointsWeight(5); // Simulation de la donnée en base
        qHard = questionRepository.save(qHard);

        AnswerOption optCorrect = new AnswerOption(null, "Bonne réponse", true);
        optCorrect.setQuestion(qHard);
        optCorrect = answerOptionRepository.save(optCorrect);

        // 3. Soumettre la bonne réponse
        String jsonAnswer = "{\"sessionId\": " + session.getId() + ", \"questionId\": " + qHard.getId() + ", \"optionId\": " + optCorrect.getId() + "}";
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer))
                .andExpect(status().isOk());

        // 4. Terminer l'examen
        MvcResult result = mockMvc.perform(post("/api/exam/finish/" + session.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 5. Vérifier que le score est bien de 5 (et pas 1)
        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        
        assertEquals(5, json.get("finalScore").asInt(), "Le score doit être de 5 pour une question HARD");
    }

    // --- TEST 3 : Blocage si temps écoulé ---
    @Test
    void testSubmitAnswer_ShouldFail_WhenTimeIsOver() throws Exception {
        // 1. Créer une session expirée (Fin il y a 10 minutes)
        ExamSession expiredSession = new ExamSession();
        expiredSession.setCandidateName("Late Candidate");
        expiredSession.setEndTime(LocalDateTime.now().minusMinutes(10)); 
        expiredSession = examSessionRepository.save(expiredSession);

        // 2. Question & Option
        Question q = new Question(); 
        q.setText("Q");
        q = questionRepository.save(q);
        
        AnswerOption opt = new AnswerOption(null, "A", true);
        opt.setQuestion(q);
        opt = answerOptionRepository.save(opt);

        // 3. Tentative de réponse
        String jsonAnswer = "{\"sessionId\": " + expiredSession.getId() + ", \"questionId\": " + q.getId() + ", \"optionId\": " + opt.getId() + "}";
        
        // 4. On s'attend à une erreur 403 Forbidden
        mockMvc.perform(post("/api/exam/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAnswer))
                .andExpect(status().isForbidden());
    }
}