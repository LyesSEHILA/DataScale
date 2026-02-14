package com.cyberscale.backend.services.rabbitmq;

import com.cyberscale.backend.services.ai.HuggingFaceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RabbitMQConsumerTest {

    @Mock
    private HuggingFaceClient huggingFaceClient;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    @Test
    void receiveMessage_Success() {
        // ARRANGE
        String message = "Generate a scenario for phishing";
        when(huggingFaceClient.generateResponse(anyString())).thenReturn("Voici un scénario...");

        // ACT
        rabbitMQConsumer.receiveMessage(message);

        // ASSERT
        verify(huggingFaceClient).generateResponse(message);
    }
}