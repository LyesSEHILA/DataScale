package com.cyberscale.backend.services.rabbitmq;

import com.cyberscale.backend.dto.GameEventDTO;
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
class RabbitMQProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQProducer producer;

    @Test
    void sendGameEvent_ShouldSendToRabbitMQ() {
        ReflectionTestUtils.setField(producer, "exchange", "test.exchange");
        ReflectionTestUtils.setField(producer, "routingKey", "test.key");

        producer.sendGameEvent("player1", "ls -la", "container1");

        verify(rabbitTemplate).convertAndSend(
                eq("test.exchange"),
                eq("test.key"),
                any(GameEventDTO.class)
        );
    }
}