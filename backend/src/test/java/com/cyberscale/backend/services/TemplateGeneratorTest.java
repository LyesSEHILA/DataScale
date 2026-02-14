package com.cyberscale.backend.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateGeneratorTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private TemplateGenerator templateGenerator;

    @Test
    void generateYaml_Success() throws IOException {
        // ARRANGE
        String mockYamlContent = "apiVersion: v1\nkind: Pod\nmetadata:\n  name: ${POD_NAME}\nenv:\n  - name: PASS\n    value: ${RANDOM_PASS}";

        // Simulation du comportement normal
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(mockYamlContent.getBytes(StandardCharsets.UTF_8)));

        // ACT
        String result = templateGenerator.generateYaml("mysql");

        // ASSERT
        assertNotNull(result);
        assertTrue(result.contains("kind: Pod"));
        
        // Vérification des remplacements
        assertFalse(result.contains("${POD_NAME}"), "Placeholder POD_NAME non remplacé");
        assertTrue(result.contains("honeypot-mysql-"), "Le nom généré est incorrect");
        
        assertFalse(result.contains("${RANDOM_PASS}"), "Placeholder RANDOM_PASS non remplacé");
    }

    @Test
    void generateYaml_FileNotFound() {
        // ARRANGE
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        // ACT & ASSERT
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            templateGenerator.generateYaml("unknown");
        });

        assertTrue(e.getMessage().contains("Template introuvable"));
    }

    // 👇 LE TEST AJOUTÉ POUR LA SÉCURITÉ 👇
    @Test
    void generateYaml_Security_PathTraversal() {
        // ARRANGE
        String maliciousType = "../../etc/passwd";

        // ACT & ASSERT
        // On s'attend à ce que le Regex bloque l'appel AVANT même de toucher au ResourceLoader
        assertThrows(IllegalArgumentException.class, () -> {
            templateGenerator.generateYaml(maliciousType);
        });

        // On vérifie que le ResourceLoader n'a JAMAIS été appelé (preuve que la sécurité est en amont)
        verify(resourceLoader, never()).getResource(anyString());
    }
}