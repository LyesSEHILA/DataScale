package com.cyberscale.backend.services.ai;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConnectionTest {

    @Bean
    CommandLineRunner testAiConnection(HuggingFaceClient aiClient) {
        return args -> {
            System.out.println("--- 🤖 TEST IA START (Mode Retry) ---");
            
            // Question simple pour tester
            String question = "quel est le président des USA ? " ; 
            
            int maxRetries = 10; // On insiste jusqu'à 10 fois (soit ~100 secondes)
            int attempt = 0;
            boolean success = false;

            while (attempt < maxRetries && !success) {
                attempt++;
                System.out.println("\n👉 Tentative " + attempt + "/" + maxRetries + " en cours...");

                // Appel à l'IA
                String response = aiClient.generateResponse(question);

                // Analyse du résultat
                if (response.contains("Erreur") || response.contains("Aucune réponse")) {
                    System.out.println("⚠️ L'IA n'est pas encore prête (Cold Start).");
                    System.out.println("⏳ Attente de 10 secondes avant réessai...");
                    
                    try {
                        Thread.sleep(10000); // Pause de 10 secondes
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // SUCCÈS !
                    System.out.println("\n✅ SUCCÈS ! L'IA a répondu :");
                    System.out.println("------------------------------------------------");
                    System.out.println(response);
                    System.out.println("------------------------------------------------");
                    success = true;
                }
            }

            if (!success) {
                System.err.println("\n❌ ÉCHEC : L'IA ne s'est pas réveillée après " + maxRetries + " tentatives.");
            }
            
            System.out.println("--- 🤖 TEST IA END ---");
        };
    }
}