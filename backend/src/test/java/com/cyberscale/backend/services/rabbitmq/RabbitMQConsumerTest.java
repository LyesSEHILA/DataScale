package com.cyberscale.backend.services.rabbitmq;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cyberscale.backend.dto.GameEventDTO;
import com.cyberscale.backend.services.ai.HuggingFaceClient;

@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @Mock private HuggingFaceClient aiClient;
    @Mock private RabbitMQProducer producer;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private RabbitMQConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new RabbitMQConsumer(aiClient, producer, messagingTemplate);
    }

    @Test
    void shouldHandleTutorialMode() {
        // GIVEN
        GameEventDTO event = new GameEventDTO("u1", "TUTORIAL|ls", "c1");
        when(aiClient.generateResponse(anyString())).thenReturn("Ceci liste les fichiers.");

        // WHEN
        consumer.receiveGameEvent(event);

        // THEN
        // Vérifie que l'IA a reçu un prompt "Instructeur"
        verify(aiClient).generateResponse(contains("Instructeur Linux"));
        // Vérifie qu'on envoie une alerte visuelle (Info)
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("ℹ️"));
        // Vérifie qu'on ne touche PAS à l'infra (pas de commande docker)
        verify(producer, never()).sendInfraCommand(anyString());
    }

    @Test
    void shouldHandleRedTeamMode() {
        // GIVEN
        GameEventDTO event = new GameEventDTO("u1", "RED_TEAM|cat /etc/passwd", "c1");
        when(aiClient.generateResponse(anyString())).thenReturn("chmod 000 /etc/passwd");

        // WHEN
        consumer.receiveGameEvent(event);

        // THEN
        // Vérifie le prompt "Blue Team (SysAdmin)"
        verify(aiClient).generateResponse(contains("Blue Team"));
        // Vérifie qu'on EXÉCUTE la riposte sur l'infra
        verify(producer).sendInfraCommand("chmod 000 /etc/passwd");
        // Vérifie l'alerte rouge
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("⚠️"));
    }

    @Test
    void shouldHandleHelpRequest() {
        // GIVEN
        GameEventDTO event = new GameEventDTO("u1", "RED_TEAM|HELP|nmap", "c1");
        when(aiClient.generateResponse(anyString())).thenReturn("nmap sert à scanner.");

        // WHEN
        consumer.receiveGameEvent(event);

        // THEN
        // Prompt Mentor
        verify(aiClient).generateResponse(contains("mentor expert"));
        // Pas d'attaque infra
        verify(producer, never()).sendInfraCommand(anyString());
        // Alerte ampoule (Conseil)
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("💡"));
    }
}