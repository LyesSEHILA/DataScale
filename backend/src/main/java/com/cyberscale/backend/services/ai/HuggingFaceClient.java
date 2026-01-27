package com.cyberscale.backend.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceClient {

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
        // MOCK PROPRE
        if (isMockEnabled) {
            System.out.println("⚠️ MOCK IA ACTIVÉ (Config)");
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
            System.err.println("🔴 ERREUR API : " + e.getResponseBodyAsString());
            return "Erreur IA (" + e.getStatusCode() + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur Technique : " + e.getMessage();
        }
        return "Aucune réponse de l'IA.";
    }
}