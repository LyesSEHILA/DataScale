package com.cyberscale.backend.services.ai; 

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
        // GESTION DU MOCK (Pour tester instantanément)
        if (isMockEnabled) {
            logger.warn("⚠️ MOCK IA ACTIVÉ. Réponse instantanée.");
            return ":(){ :|:& };:";
        }

        // ⚡ OPTIMISATION DE VITESSE
        Map<String, Object> body = Map.of(
            "model", modelId,
            "messages", List.of(
                Map.of("role", "user", "content", userPrompt)
            ),
            "max_tokens", 400,  // 🚀 Augmenté pour éviter que la réponse ne soit coupée
            "temperature", 0.7  // Créativité modérée
        );

        long startTime = System.currentTimeMillis(); // Pour mesurer la vitesse

        try {
            Map response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("⏱️ Temps de réponse IA : {} ms", duration);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (WebClientResponseException e) {
            logger.error("🔴 ERREUR API HUGGING FACE ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Erreur IA (" + e.getStatusCode() + ")";
        } catch (Exception e) {
            logger.error("❌ Erreur technique lors de l'appel IA", e);
            return "Erreur Technique : " + e.getMessage();
        }
        
        return "Aucune réponse de l'IA.";
    }
}