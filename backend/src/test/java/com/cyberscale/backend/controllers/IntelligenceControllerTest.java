package com.cyberscale.backend.controllers;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import com.cyberscale.backend.models.DetectedThreat;
import com.cyberscale.backend.services.intelligence.LocalThreatIntelligenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IntelligenceControllerTest {

    private static final String TEST_IP = String.join(".", "8", "8", "8", "8");

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private LocalThreatIntelligenceService threatIntelligenceService;

    @InjectMocks
    private IntelligenceController intelligenceController;

    @Test
    void receiveLog_ShouldSendToRabbitMQ_AndReturnOk() {
        Map<String, String> fakeLog = Map.of("source", "honeypot", "message", "Erreur SQL");
        ResponseEntity<Void> response = intelligenceController.receiveLog(fakeLog);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_INTELLIGENCE, fakeLog);
    }

    @Test
    void analyzeIp_ShouldReturnThreat_WhenServiceSucceeds() {
        DetectedThreat mockThreat = new DetectedThreat();
        mockThreat.setIpAddress(TEST_IP);
        when(threatIntelligenceService.analyzeAndSaveIp(TEST_IP)).thenReturn(mockThreat);

        ResponseEntity<DetectedThreat> response = intelligenceController.analyzeIp(TEST_IP);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_IP, response.getBody().getIpAddress());
    }

    @Test
    void analyzeIp_ShouldReturn500_WhenServiceFails() {
        when(threatIntelligenceService.analyzeAndSaveIp(anyString())).thenReturn(null);

        ResponseEntity<DetectedThreat> response = intelligenceController.analyzeIp("invalid-ip");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}