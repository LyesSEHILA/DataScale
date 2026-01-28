package com.cyberscale.backend.services.ai;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import okhttp3.mockwebserver.SocketPolicy;

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
        
        // Configuration via Reflection
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
        String jsonBody = "{\"choices\": [{\"message\": {\"role\": \"assistant\", \"content\": \"rm -rf /\"}}]}";
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonBody)
                .addHeader("Content-Type", "application/json"));

        String response = client.generateResponse("destroy everything");
        assertEquals("rm -rf /", response);
    }

    @Test
    void shouldReturnError_WhenApiReturns400() {
        // GIVEN : L'API renvoie une 400
        // IMPORTANT : On renvoie un JSON vide "{}" et le bon Header.
        // Sinon WebClient plante en essayant de lire "Bad Request" comme du JSON,
        // et on tombe dans le mauvais bloc catch (Erreur Technique).
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{}") 
                .addHeader("Content-Type", "application/json"));

        // WHEN
        String response = client.generateResponse("test");

        // DEBUG : Affiche ce qu'on a reçu pour comprendre si ça re-plante
        System.out.println("Réponse reçue dans le test : " + response);

        // THEN : On vérifie qu'on a bien traité l'erreur HTTP
        // On vérifie les morceaux séparément pour être plus souple sur le formatage
        assertTrue(response.contains("Erreur IA"), "La réponse doit mentionner 'Erreur IA'");
        assertTrue(response.contains("400"), "La réponse doit contenir le code 400");
    }

    @Test
    void shouldReturnFallback_WhenChoicesAreEmpty() {
        // Test du cas où l'API répond mais sans "choices" (liste vide)
        String jsonBody = "{\"choices\": []}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonBody)
                .addHeader("Content-Type", "application/json"));

        String response = client.generateResponse("test");
        assertEquals("Aucune réponse de l'IA.", response);
    }

    @Test
    void shouldUseMock_WhenConfigured() {
        setField(client, "isMockEnabled", true);
        String response = client.generateResponse("test");
        assertTrue(response.contains("Commande simulée"));
    }
    
   @Test
    void shouldReturnError_WhenTechnicalError() {
        // GIVEN
        // On simule une erreur de connexion réseau (ex: le serveur est down)
        mockWebServer.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)); 

        // WHEN
        String response = client.generateResponse("test");

        // THEN
        // On vérifie qu'on a un message d'erreur générique
        // Le message exact peut varier ("Erreur Technique", "Connection reset", etc.)
        // L'important est que ça ne crashe pas et que ça renvoie une erreur.
        assertNotNull(response);
        // On accepte soit "Erreur Technique", soit un message d'erreur réseau
        boolean isError = response.contains("Erreur") || response.contains("Error") || response.contains("Exception");
        assertTrue(isError, "La réponse devrait indiquer une erreur (Reçu: " + response + ")");
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