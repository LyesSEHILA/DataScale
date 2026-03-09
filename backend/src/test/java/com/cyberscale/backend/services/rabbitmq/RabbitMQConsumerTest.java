package com.cyberscale.backend.services.rabbitmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cyberscale.backend.dto.GameEventDTO;
import com.cyberscale.backend.services.ai.HuggingFaceClient;

@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @Mock
    private HuggingFaceClient aiClient;

    @Mock
    private RabbitMQProducer producer;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    private GameEventDTO event;

    @BeforeEach
    void setUp() {
        event = new GameEventDTO();
    }

    @Test
    void testReceiveGameEvent_NoPipe_DefaultsToTutorial() {
        // Test quand le message n'a pas de pipe "|"
        event.setAction("ls -la");
        when(aiClient.generateResponse(anyString())).thenReturn("Explication de la commande");

        rabbitMQConsumer.receiveGameEvent(event);

        verify(aiClient).generateResponse(anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("ℹ️ INFO :"));
        verifyNoInteractions(producer);
    }

    @Test
    void testReceiveGameEvent_HelpGeneralContext() {
        // Test de l'aide générale (isHelpRequest = true, command = GENERAL_CONTEXT)
        event.setAction("RED_TEAM|HELP|GENERAL_CONTEXT");
        when(aiClient.generateResponse(anyString())).thenReturn("```bash\nVoici l'objectif général\n```");

        rabbitMQConsumer.receiveGameEvent(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("💡 CONSEIL :"));
        verifyNoInteractions(producer);
    }

    @Test
    void testReceiveGameEvent_HelpEmptyCommand() {
        // Test de l'aide générale (isHelpRequest = true, command = "")
        event.setAction("BLUE_TEAM|HELP|");
        when(aiClient.generateResponse(anyString())).thenReturn("Voici l'objectif général");

        rabbitMQConsumer.receiveGameEvent(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("💡 CONSEIL :"));
    }

    @Test
    void testReceiveGameEvent_HelpSpecificCommand() {
        // Test de l'aide sur une commande spécifique
        event.setAction("RED_TEAM|HELP|nmap -sS");
        when(aiClient.generateResponse(anyString())).thenReturn("Explication sur nmap");

        rabbitMQConsumer.receiveGameEvent(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("💡 CONSEIL :"));
    }

    @Test
    void testReceiveGameEvent_RedTeam_ValidParsing() {
        // Test du mode combat avec parsing correct (COMMANDE et EXPLICATION présents)
        event.setAction("RED_TEAM|nmap");
        String aiResponse = "COMMANDE: iptables -A INPUT -j DROP EXPLICATION: Blocage de l'IP";
        when(aiClient.generateResponse(anyString())).thenReturn(aiResponse);

        rabbitMQConsumer.receiveGameEvent(event);

        // Vérifie l'envoi vers l'infrastructure
        verify(producer).sendInfraCommand("iptables -A INPUT -j DROP");
        // Vérifie l'envoi formaté au WebSocket
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("⚠️ ACTION ADVERSE"));
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("💻 iptables -A INPUT -j DROP"));
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains("📖 Blocage de l'IP"));
    }

   @Test
    void testReceiveGameEvent_BlueTeam_ParsingException() {
        // Test du catch interne lors du parsing (ArrayIndexOutOfBoundsException)
        event.setAction("BLUE_TEAM|ls");
        
        // L'absence de texte APRÈS "EXPLICATION:" va faire planter le parts[1]
        String aiResponse = "COMMANDE: rm -rf EXPLICATION:"; 
        when(aiClient.generateResponse(anyString())).thenReturn(aiResponse);

        rabbitMQConsumer.receiveGameEvent(event);

        // LE FIX EST ICI : Même si le parsing global échoue, le code réussit 
        // à extraire "rm -rf" avant le catch. On vérifie donc qu'il envoie bien ça !
        verify(producer).sendInfraCommand("rm -rf");
    }

    @Test
    void testReceiveGameEvent_BlueTeam_NoParsingTags() {
        // Test du mode combat mais l'IA n'a pas respecté le format (pas de tags)
        event.setAction("BLUE_TEAM|whoami");
        String aiResponse = "Je vais utiliser la commande ls"; 
        when(aiClient.generateResponse(anyString())).thenReturn(aiResponse);

        rabbitMQConsumer.receiveGameEvent(event);

        // La commande brute est envoyée sans plantage
        verify(producer).sendInfraCommand(aiResponse);
        verify(messagingTemplate).convertAndSend(eq("/topic/arena/alerts"), contains(aiResponse));
    }

    @Test
    void testReceiveGameEvent_DefaultMode() {
        // Test du default du switch case
        event.setAction("UNKNOWN_MODE|ls");
        when(aiClient.generateResponse(anyString())).thenReturn("Commande par defaut");

        rabbitMQConsumer.receiveGameEvent(event);

        verify(producer).sendInfraCommand("Commande par defaut");
    }

    @Test
    void testReceiveGameEvent_AiClientThrowsException() {
        // Test de l'exception globale de l'IA (API down par exemple)
        event.setAction("TUTORIAL|ls");
        when(aiClient.generateResponse(anyString())).thenThrow(new RuntimeException("Hugging Face API Down"));

        rabbitMQConsumer.receiveGameEvent(event);

        // L'exception est catchée, le flow s'arrête sans crasher le système
        verifyNoInteractions(producer);
        verifyNoInteractions(messagingTemplate);
    }
}