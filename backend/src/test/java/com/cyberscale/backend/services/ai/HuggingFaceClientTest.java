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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HuggingFaceClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HuggingFaceClient huggingFaceClient;

    @BeforeEach
    void setUp() {
        // 1. Configurer le Builder pour qu'il retourne notre mock WebClient
        when(webClientBuilder.build()).thenReturn(webClient);

        // 2. Instancier le service manuellement
        huggingFaceClient = new HuggingFaceClient(webClientBuilder);

        // 3. Injecter les valeurs @Value manuellement
        ReflectionTestUtils.setField(huggingFaceClient, "apiUrl", "https://api-fake.com");
        ReflectionTestUtils.setField(huggingFaceClient, "apiKey", "dummy_key");
        ReflectionTestUtils.setField(huggingFaceClient, "modelId", "test-model");
        ReflectionTestUtils.setField(huggingFaceClient, "isMockEnabled", false);
    }

    @Test
    void generateResponse_Success() {
        // ARRANGE
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        
        // Cette ligne gère TOUS les appels à .header() (Authorization ET Content-Type)
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        
        // --- SUPPRESSION DE LA LIGNE contentType() QUI CAUSAIT L'ERREUR ---
        // when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);

        when(requestBodySpec.bodyValue(any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Simulation de la réponse de l'API
        Map<String, Object> mockResponse = Map.of(
            "choices", List.of(
                Map.of("message", Map.of("content", "AI Response"))
            )
        );

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(mockResponse));

        // ACT
        String response = huggingFaceClient.generateResponse("Hello");

        // ASSERT
        assertEquals("AI Response", response);
    }
}