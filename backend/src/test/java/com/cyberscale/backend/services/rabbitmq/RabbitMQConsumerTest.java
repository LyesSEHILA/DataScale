package com.cyberscale.backend.services.rabbitmq;

import com.cyberscale.backend.dto.GameEventDTO;
import com.cyberscale.backend.services.ai.HuggingFaceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RabbitMQConsumerTest {

    @Mock
    private HuggingFaceClient huggingFaceClient;

    @Mock
    private RabbitMQProducer producer;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    @Test
    void receiveGameEvent_HelpRequest_Success() {
        // ARRANGE
        // On simule un DTO entrant (comme s'il venait de la queue RabbitMQ)
        GameEventDTO event = new GameEventDTO("user1", "HELP|ls", "container_123");
        
        when(huggingFaceClient.generateResponse(anyString())).thenReturn("Ceci est une commande pour lister les fichiers.");

        // ACT
        rabbitMQConsumer.receiveGameEvent(event);

        // ASSERT
        verify(huggingFaceClient).generateResponse(anyString());
        // Vérifie qu'on envoie bien la réponse au WebSocket
        verify(messagingTemplate).convertAndSend(anyString(), anyString()); 
    }
}