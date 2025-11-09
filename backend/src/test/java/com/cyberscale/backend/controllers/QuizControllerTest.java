package com.cyberscale.backend.controllers;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Charge tout le contexte Spring Boot
@AutoConfigureMockMvc // Nous donne l'outil 'MockMvc' pour appeler nos API
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc; // L'outil pour simuler les appels API

    @Autowired
    private ObjectMapper objectMapper; // Un outil pour convertir nos objets Java en JSON

    @Test
    void testStartQuiz_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // 1. Préparation (Arrange)
        // Crée une requête valide
        OnboardingRequest request = new OnboardingRequest(25L, 5L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);

        // 2. Action (Act) & 3. Vérification (Assert)
        mockMvc.perform(post("/api/quiz/start") // Appelle POST /api/quiz/start
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated()) // On s'attend à un statut 201 CREATED
            .andExpect(jsonPath("$.id").exists()) // On vérifie que la réponse contient un ID
            .andExpect(jsonPath("$.age").value(25)) // On vérifie que l'âge est correct
            .andExpect(jsonPath("$.selfEvalTheory").value(5));
    }

    @Test
    void testStartQuiz_ShouldReturn400_WhenAgeIsInvalid() throws Exception {
        // 1. Préparation (Arrange)
        // Crée une requête invalide (âge = 0, mais le DTO exige min=1)
        OnboardingRequest request = new OnboardingRequest(0L, 5L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);

        // 2. Action (Act) & 3. Vérification (Assert)
        mockMvc.perform(post("/api/quiz/start") // Appelle POST /api/quiz/start
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest()); // On s'attend à un statut 400 BAD REQUEST
    }
    
    @Test
    void testStartQuiz_ShouldReturn400_WhenTheoryEvalIsInvalid() throws Exception {
        // 1. Préparation (Arrange)
        // Crée une requête invalide (évaluation = 11, mais le DTO exige max=10)
        OnboardingRequest request = new OnboardingRequest(25L, 11L, 7L);
        String requestJson = objectMapper.writeValueAsString(request);

        // 2. Action (Act) & 3. Vérification (Assert)
        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest()); // On s'attend à un statut 400 BAD REQUEST
    }
}