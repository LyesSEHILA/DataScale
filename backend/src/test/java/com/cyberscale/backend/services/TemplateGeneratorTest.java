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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemplateGeneratorTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private TemplateGenerator templateGenerator;

    @Test
    void generateYaml_Success() throws IOException {
        String mockYamlContent = "apiVersion: v1\nkind: Pod\nmetadata:\n  name: ${POD_NAME}\nenv:\n  - name: PASS\n    value: ${RANDOM_PASS}";

        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(mockYamlContent.getBytes(StandardCharsets.UTF_8)));

        String result = templateGenerator.generateYaml("mysql");

        assertNotNull(result);
        assertTrue(result.contains("kind: Pod"));
        assertFalse(result.contains("${POD_NAME}"), "Placeholder POD_NAME non remplacé");
        assertTrue(result.contains("honeypot-mysql-"));
        assertFalse(result.contains("${RANDOM_PASS}"), "Placeholder RANDOM_PASS non remplacé");
    }

    @Test
    void generateYaml_FileNotFound() {
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            templateGenerator.generateYaml("unknown_type");
        });

        assertTrue(e.getMessage().contains("Template introuvable"));
    }
}