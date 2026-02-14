package com.cyberscale.backend.services.rabbitmq;

import com.cyberscale.backend.dto.GameEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitMQProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQProducer rabbitMQProducer;

    @BeforeEach
    void setUp() {
        // On injecte les valeurs des propriétés @Value car elles sont nulles dans un test unitaire simple
        ReflectionTestUtils.setField(rabbitMQProducer, "exchange", "test.exchange");
        ReflectionTestUtils.setField(rabbitMQProducer, "routingKey", "test.key");
        ReflectionTestUtils.setField(rabbitMQProducer, "infraRoutingKey", "test.infra.key");
    }

    @Test
    void sendGameEvent_Success() {
        // ARRANGE
        String playerId = "user1";
        String action = "ls -la";
        String containerId = "container_123";

        // ACT
        rabbitMQProducer.sendGameEvent(playerId, action, containerId);

        // ASSERT
        // On vérifie que la méthode convertAndSend est appelée avec un objet GameEventDTO
        verify(rabbitTemplate).convertAndSend(eq("test.exchange"), eq("test.key"), any(GameEventDTO.class));
    }

    @Test
    void sendInfraCommand_Success() {
        // ARRANGE
        String command = "block ip 192.168.1.50";

        // ACT
        rabbitMQProducer.sendInfraCommand(command);

        // ASSERT
        verify(rabbitTemplate).convertAndSend(eq("test.exchange"), eq("test.infra.key"), eq(command));
    }
}