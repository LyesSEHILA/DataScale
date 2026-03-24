package com.cyberscale.backend.controllers;

import com.cyberscale.backend.config.SecurityConfig;
import com.cyberscale.backend.models.DetectedThreat;
import com.cyberscale.backend.services.intelligence.LocalThreatIntelligenceService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class IntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private LocalThreatIntelligenceService threatIntelligenceService;

    @Test
    void receiveLog_ShouldSendToRabbitMQ() throws Exception {
        String jsonLog = "{\"ip\": \"1.1.1.1\", \"message\": \"Tentative SSH\"}";

        mockMvc.perform(post("/api/intelligence/log")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonLog))
                .andExpect(status().isOk());

        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Map.class));
    }

    @Test
    void analyzeIp_ShouldReturnThreatAnalysis() throws Exception {
        DetectedThreat mockThreat = new DetectedThreat();
        mockThreat.setIpAddress("1.1.1.1");
        mockThreat.setCountryCode("FR");
        mockThreat.setAbuseConfidenceScore(85);
        mockThreat.setDetectedAt(LocalDateTime.now());

        when(threatIntelligenceService.analyzeAndSaveIp(eq("1.1.1.1"))).thenReturn(mockThreat);

        mockMvc.perform(get("/api/intelligence/analyze-ip")
                .param("ip", "1.1.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ipAddress").value("1.1.1.1"))
                .andExpect(jsonPath("$.countryCode").value("FR"))
                .andExpect(jsonPath("$.abuseConfidenceScore").value(85));
    }

    @Test
    void analyzeIp_ShouldHandleError_WhenServiceReturnsNull() throws Exception {
        when(threatIntelligenceService.analyzeAndSaveIp(any())).thenReturn(null);

        mockMvc.perform(get("/api/intelligence/analyze-ip")
                .param("ip", "unknown"))
                .andExpect(status().isInternalServerError());
    }
}