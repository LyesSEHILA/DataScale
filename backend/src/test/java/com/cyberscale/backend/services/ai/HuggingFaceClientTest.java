package com.cyberscale.backend.services.ai;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class HuggingFaceClientTest {

    private MockWebServer mockWebServer;
    private HuggingFaceClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        client = new HuggingFaceClient(WebClient.builder());
        
        // Injection via Réflexion (car on n'utilise pas tout Spring Boot ici pour aller vite)
        setField(client, "apiUrl", mockWebServer.url("/").toString());
        setField(client, "apiKey", "fake-key");
        setField(client, "modelId", "fake-model");
        setField(client, "isMockEnabled", false);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnCommand_WhenApiRespondsSuccess() {
        // JSON simulé (réponse type OpenAI/Zephyr)
        String jsonBody = "{\"choices\": [{\"message\": {\"role\": \"assistant\", \"content\": \"rm -rf /\"}}]}";
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonBody)
                .addHeader("Content-Type", "application/json"));

        String response = client.generateResponse("destroy everything");
        assertEquals("rm -rf /", response);
    }

    @Test
    void shouldUseMock_WhenConfigured() {
        setField(client, "isMockEnabled", true); // On active le mock

        String response = client.generateResponse("test");
        
        assertTrue(response.contains("Commande simulée"));
        assertEquals(0, mockWebServer.getRequestCount()); // Aucune requête réseau ne doit partir
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}