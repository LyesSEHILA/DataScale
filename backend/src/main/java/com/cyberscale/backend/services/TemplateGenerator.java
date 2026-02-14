package com.cyberscale.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class TemplateGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TemplateGenerator.class);
    
    private static final String TEMPLATE_PATH = "classpath:templates/k8s/";
    
    private final ResourceLoader resourceLoader;

    public TemplateGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String generateYaml(String type) throws IOException {
        String filename = "honeypot-" + type + ".yaml";
        
        Resource resource = resourceLoader.getResource(TEMPLATE_PATH + filename);
        
        if (!resource.exists()) {
            logger.error("Template not found for type: {}", type);
            throw new IllegalArgumentException("Template introuvable pour le type : " + type);
        }

        String templateContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String podName = "honeypot-" + type + "-" + uuid;
        String randomPass = generateRandomPassword(12);

        String finalYaml = templateContent
                .replace("${POD_NAME}", podName)
                .replace("${RANDOM_PASS}", randomPass);

        return finalYaml;
    }

    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }
}