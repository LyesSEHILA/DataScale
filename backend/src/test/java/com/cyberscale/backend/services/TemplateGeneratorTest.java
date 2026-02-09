package com.cyberscale.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TemplateGeneratorTest {

    @Mock
    private ResourceLoader resourceLoader; // On mocke le chargeur

    @Mock
    private Resource resource; // On mocke le fichier lui-même

    @InjectMocks
    private TemplateGenerator templateGenerator;
@Test
    void generateYaml_Success() throws IOException {
        // ARRANGE
        // Contenu fictif du fichier YAML pour le test
        String mockYamlContent = "apiVersion: v1\nkind: Pod\nmetadata:\n  name: ${POD_NAME}\nenv:\n  - name: PASS\n    value: ${RANDOM_PASS}";

        // On dit : "Quand on cherche n'importe quel fichier..."
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        // "... dis qu'il existe"
        when(resource.exists()).thenReturn(true);
        // "... et renvoie mon contenu fictif quand on le lit"
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(mockYamlContent.getBytes(StandardCharsets.UTF_8)));

        // ACT
        String result = templateGenerator.generateYaml("mysql");

        // ASSERT
        assertNotNull(result);

        // Vérifie que le contenu est là
        assertTrue(result.contains("kind: Pod"));

        // Vérifie que le nom du POD a été généré et remplacé
        assertFalse(result.contains("${POD_NAME}"), "Le placeholder doit être remplacé");
        assertTrue(result.contains("honeypot-mysql-"), "Le nom généré doit être correct");

        // Vérifie que le mot de passe a été généré et remplacé
        // (Ceci valide implicitement la méthode privée generateRandomPassword !)
        assertFalse(result.contains("${RANDOM_PASS}"), "Le mot de passe doit être remplacé");
    }

    @Test
    void generateYaml_FileNotFound() {
        // ARRANGE
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(false); // Cette fois, le fichier n'existe pas

        // ACT & ASSERT
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            templateGenerator.generateYaml("unknown_type");
        });

        assertEquals("Template introuvable pour le type : unknown_type", e.getMessage());
    }
}