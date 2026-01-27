package com.cyberscale.backend.services.ai; // Attention au package "services" (pluriel)

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceClient {

    // 1. On déclare le Logger (c'est lui qui va écrire les messages proprement)
    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceClient.class);

    private final WebClient webClient;

    @Value("${ai.huggingface.api-url}")
    private String apiUrl;

    @Value("${ai.huggingface.api-key}")
    private String apiKey;

    @Value("${ai.huggingface.model}")
    private String modelId;

    @Value("${ai.mock.enabled:false}")
    private boolean isMockEnabled;

    public HuggingFaceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateResponse(String userPrompt) {
        // GESTION DU MOCK (Avec Logger)
        if (isMockEnabled) {
            logger.warn("⚠️ MOCK IA ACTIVÉ par configuration. Aucune requête API ne sera envoyée.");
            return "echo 'Commande simulée par le Mock'; ls -la";
        }

        Map<String, Object> body = Map.of(
            "model", modelId,
            "messages", List.of(
                Map.of("role", "user", "content", "Give me a linux command to " + userPrompt)
            )
        );

        try {
            Map response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (WebClientResponseException e) {
            // 2. On remplace System.err par logger.error
            logger.error("🔴 ERREUR API HUGGING FACE ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Erreur IA (" + e.getStatusCode() + ")";
        } catch (Exception e) {
            // 3. On remplace e.printStackTrace() par logger.error avec l'exception
            logger.error("❌ Erreur technique lors de l'appel IA", e);
            return "Erreur Technique : " + e.getMessage();
        }
        
        logger.warn("L'IA n'a renvoyé aucune réponse valide.");
        return "Aucune réponse de l'IA.";
    }
}