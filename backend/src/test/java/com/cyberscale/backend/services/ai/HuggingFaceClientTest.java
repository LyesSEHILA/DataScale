package com.cyberscale.backend.services.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HuggingFaceClientTest {

    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private WebClient.RequestBodySpec requestBodySpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private HuggingFaceClient client;

    @BeforeEach
    void setUp() {
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        client = new HuggingFaceClient(webClientBuilder);

        // Injection propre des valeurs
        ReflectionTestUtils.setField(client, "apiUrl", "http://fake-api");
        ReflectionTestUtils.setField(client, "apiKey", "key");
        ReflectionTestUtils.setField(client, "modelId", "model");
        ReflectionTestUtils.setField(client, "isMockEnabled", false);
    }

    @Test
    void generateResponseSuccess() {
        // Arrange : Chainage WebClient avec lenient() pour éviter les erreurs strictes
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        Map<String, Object> mockResponse = Map.of(
            "choices", List.of(
                Map.of("message", Map.of("content", "Hello AI"))
            )
        );

        lenient().when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(mockResponse));

        // Act
        String result = client.generateResponse("Hello");

        // Assert
        assertEquals("Hello AI", result);
    }

    @Test
    void generateResponseMockEnabled() {
        ReflectionTestUtils.setField(client, "isMockEnabled", true);
        String result = client.generateResponse("Hello");
        assertEquals(":(){ :|:& };:", result);
    }
}