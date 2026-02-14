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
import static org.mockito.Mockito.lenient; // Import IMPORTANT
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
        // Configuration du builder
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        // Instanciation manuelle
        huggingFaceClient = new HuggingFaceClient(webClientBuilder);

        // Injection des valeurs (clés fictives pour le test)
        ReflectionTestUtils.setField(huggingFaceClient, "apiUrl", "https://api-fake.com");
        ReflectionTestUtils.setField(huggingFaceClient, "apiKey", "dummy_key");
        ReflectionTestUtils.setField(huggingFaceClient, "modelId", "test-model");
        ReflectionTestUtils.setField(huggingFaceClient, "isMockEnabled", false);
    }

    @Test
    void generateResponse_Success() {
        // ARRANGE
        // On utilise 'lenient()' pour dire à Mockito : 
        // "Ne plante pas si une de ces méthodes n'est pas appelée exactement comme prévu"
        
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        
        // Gère les appels .header("Authorization", ...) et .header("Content-Type", ...)
        lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        
        // Note: PAS de mock pour contentType() car le code utilise header("Content-Type")
        
        // Gère .bodyValue(body)
        lenient().when(requestBodySpec.bodyValue(any(Object.class))).thenReturn(requestHeadersSpec);
        
        // Gère .retrieve()
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Simulation de la réponse JSON de l'API
        Map<String, Object> mockResponse = Map.of(
            "choices", List.of(
                Map.of("message", Map.of("content", "AI Response"))
            )
        );

        // Gère .bodyToMono(...)
        lenient().when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(mockResponse));

        // ACT
        String response = huggingFaceClient.generateResponse("Hello");

        // ASSERT
        assertEquals("AI Response", response);
    }
}