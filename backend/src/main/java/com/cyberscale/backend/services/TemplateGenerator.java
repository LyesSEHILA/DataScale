package com.cyberscale.backend.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class TemplateGenerator {

    private static final String TEMPLATE_PATH = "templates/k8s/";

    /**
     * Charge un template K8S et remplace les variables dynamiques.
     * @param type Le type de honeypot (ex: "mysql", "nginx")
     * @return Le contenu YAML final sous forme de String
     */
    public String generateYaml(String type) throws IOException {
        // 1. Lire le fichier template depuis les resources
        String filename = "honeypot-" + type + ".yaml";
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH + filename);
        
        if (!resource.exists()) {
            throw new IllegalArgumentException("Template introuvable pour le type : " + type);
        }

        String templateContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // 2. Générer les valeurs uniques
        String uuid = UUID.randomUUID().toString().substring(0, 8); // On garde juste 8 chars pour lisibilité
        String podName = "honeypot-" + type + "-" + uuid;
        String randomPass = generateRandomPassword(12);

        // 3. Remplacer les placeholders
        String finalYaml = templateContent
                .replace("${POD_NAME}", podName)
                .replace("${RANDOM_PASS}", randomPass);

        return finalYaml;
    }

    /**
     * Génère un mot de passe aléatoire sécurisé.
     */
    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }
}