package com.cyberscale.backend.services.rabbitmq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitMQProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQProducer rabbitMQProducer;

    @Test
    void sendMessage_Success() {
        // ARRANGE
        String message = "Test Message";
        String exchange = "cyberscale.exchange";
        String routingKey = "cyberscale.key";

        // ACT
        rabbitMQProducer.sendMessage(message);

        // ASSERT
        // Vérifie que convertAndSend est appelé avec les bons paramètres
        // Note: Assurez-vous que RabbitMQProducer utilise bien ces constantes ou qu'elles sont injectées
        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(routingKey), eq(message));
    }
}