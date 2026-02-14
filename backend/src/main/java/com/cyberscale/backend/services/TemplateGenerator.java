package com.cyberscale.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TemplateGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TemplateGenerator.class);
    
    // Le "classpath:" est important pour que ça marche aussi bien en IDE que dans le JAR final
    private static final String TEMPLATE_PATH = "classpath:templates/k8s/";
    
    // SÉCURITÉ : Regex pour empêcher le "Path Traversal" (ex: ../../etc/passwd)
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    private final ResourceLoader resourceLoader;
    private final SecureRandom secureRandom; // On le garde en attribut pour la perf

    public TemplateGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.secureRandom = new SecureRandom();
    }

    public String generateYaml(String type) throws IOException {
        // 1. SÉCURITÉ : Validation de l'entrée
        if (!SAFE_FILENAME_PATTERN.matcher(type).matches()) {
            logger.warn("Tentative de Path Traversal détectée avec le type: {}", type);
            throw new IllegalArgumentException("Type de template invalide (caractères alphanumériques uniquement).");
        }

        String filename = "honeypot-" + type + ".yaml";
        Resource resource = resourceLoader.getResource(TEMPLATE_PATH + filename);
        
        if (!resource.exists()) {
            logger.error("Template introuvable: {}", filename);
            throw new IllegalArgumentException("Template introuvable pour le type : " + type);
        }

        // 2. ROBUSTESSE : Try-with-resources pour fermer le flux
        String templateContent;
        try (InputStream inputStream = resource.getInputStream()) {
            templateContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }

        // 3. GÉNÉRATION
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String podName = "honeypot-" + type + "-" + uuid;
        String randomPass = generateRandomPassword(12);

        logger.info("Génération template pour {} (Pod: {})", type, podName);

        return templateContent
                .replace("${POD_NAME}", podName)
                .replace("${RANDOM_PASS}", randomPass);
    }

    private String generateRandomPassword(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes); // Utilise l'instance unique
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }
}