package com.cyberscale.backend.controllers;

import com.cyberscale.backend.services.PhishingService;
import com.cyberscale.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 3.4+
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécurité pour tester l'API pure
@Import(SecurityConfig.class)
class PhishingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PhishingService phishingService;

    // --- Test: POST /api/phishing/analyze ---

    @Test
    void analyzeClick_ShouldReturnSuccessResponse() throws Exception {
        // 1. Mock du service
        when(phishingService.analyzeClick(eq("S1"), eq("trap-btn")))
                .thenReturn(Map.of("isTrap", true, "message", "Bravo !"));

        // 2. Requête JSON
        String jsonRequest = "{\"scenarioId\": \"S1\", \"elementId\": \"trap-btn\"}";

        // 3. Appel & Vérification
        mockMvc.perform(post("/api/phishing/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isTrap").value(true))
                .andExpect(jsonPath("$.message").value("Bravo !"));
    }

    @Test
    void analyzeClick_ShouldHandleSafeElement() throws Exception {
        when(phishingService.analyzeClick(eq("S1"), eq("safe-text")))
                .thenReturn(Map.of("isTrap", false, "message", "Raté"));

        String jsonRequest = "{\"scenarioId\": \"S1\", \"elementId\": \"safe-text\"}";

        mockMvc.perform(post("/api/phishing/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isTrap").value(false));
    }

    // --- Test: GET /api/phishing/info/{id} ---

    @Test
    void getScenarioInfo_ShouldReturnDetails() throws Exception {
        when(phishingService.getScenarioInfo("S1"))
                .thenReturn(Map.of("totalTraps", 5, "lesson", "Attention aux liens"));

        mockMvc.perform(get("/api/phishing/info/S1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTraps").value(5))
                .andExpect(jsonPath("$.lesson").value("Attention aux liens"));
    }
}