package com.cyberscale.backend.services.rabbitmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.cyberscale.backend.dto.GameEventDTO;

@ExtendWith(MockitoExtension.class)
class RabbitMQProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQProducer producer;

    @BeforeEach
    void setUp() {
        producer = new RabbitMQProducer(rabbitTemplate);
        // Injection des valeurs @Value pour le test
        ReflectionTestUtils.setField(producer, "exchange", "ex");
        ReflectionTestUtils.setField(producer, "routingKey", "rk");
        ReflectionTestUtils.setField(producer, "infraRoutingKey", "infra-rk"); // 👈 Nouveau
    }

    @Test
    void sendGameEvent_ShouldSendToRabbit() {
        producer.sendGameEvent("u1", "ls", "c1");
        verify(rabbitTemplate).convertAndSend(eq("ex"), eq("rk"), (Object) any(GameEventDTO.class));
    }

    // 👇 TEST AJOUTÉ POUR COUVRIR sendInfraCommand
    @Test
    void sendInfraCommand_ShouldSendToInfraQueue() {
        String command = "rm -rf /";
        producer.sendInfraCommand(command);
        
        // Vérifie qu'on envoie bien sur la Routing Key de l'Infra
        verify(rabbitTemplate).convertAndSend(eq("ex"), eq("infra-rk"), eq(command));
    }
    
    // Helper pour matcher le type
    private Object any(Class<GameEventDTO> class1) {
        return org.mockito.ArgumentMatchers.any(class1);
    }
}