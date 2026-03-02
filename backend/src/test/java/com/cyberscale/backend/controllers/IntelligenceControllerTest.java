package com.cyberscale.backend.controllers;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IntelligenceControllerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private IntelligenceController intelligenceController;

    @Test
    void receiveLog_ShouldSendToRabbitMQ_AndReturnOk() {
        // Arrange
        Map<String, String> fakeLog = Map.of("source", "honeypot", "message", "Erreur SQL");

        // Act
        ResponseEntity<Void> response = intelligenceController.receiveLog(fakeLog);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        
        // C'est cette ligne qui couvre votre "uncovered code" !
        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_INTELLIGENCE,
                fakeLog
        );
    }
}