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
        assertEquals(200, response.getStatusCodeValue());
        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_INTELLIGENCE, fakeLog);
    }

    @Test
    void analyzeIp_ShouldReturnThreat_WhenServiceSucceeds() {
        DetectedThreat mockThreat = new DetectedThreat();
        mockThreat.setIpAddress("8.8.8.8");
        when(threatIntelligenceService.analyzeAndSaveIp("8.8.8.8")).thenReturn(mockThreat);

        ResponseEntity<DetectedThreat> response = intelligenceController.analyzeIp("8.8.8.8");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("8.8.8.8", response.getBody().getIpAddress());
    }
}