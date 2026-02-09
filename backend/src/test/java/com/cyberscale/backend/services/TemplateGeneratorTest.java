package com.cyberscale.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TemplateGeneratorTest {

    private TemplateGenerator templateGenerator;

    @BeforeEach
    void setUp() {
        // La classe n'a pas de dépendances, on l'instancie directement
        templateGenerator = new TemplateGenerator();
    }

    @Test
    void generateYaml_Success() throws IOException {
        // ARRANGE
        // On utilise le type "test" qui va chercher "honeypot-test.yaml" (créé à l'étape 1)
        String type = "test";

        // ACT
        String result = templateGenerator.generateYaml(type);

        // ASSERT
        assertNotNull(result);

        // 1. Vérifier que le contenu du fichier a bien été lu
        assertTrue(result.contains("kind: Pod"), "Le contenu du fichier doit être présent");

        // 2. Vérifier que ${POD_NAME} a été remplacé
        assertFalse(result.contains("${POD_NAME}"), "Le placeholder ${POD_NAME} doit être remplacé");
        assertTrue(result.contains("honeypot-test-"), "Le nom du pod doit contenir le préfixe généré");

        // 3. Vérifier que ${RANDOM_PASS} a été remplacé (couvre la méthode privée generateRandomPassword)
        assertFalse(result.contains("${RANDOM_PASS}"), "Le placeholder ${RANDOM_PASS} doit être remplacé");
    }

    @Test
    void generateYaml_FileNotFound_ShouldThrowException() {
        // ARRANGE
        String unknownType = "inexistant-12345";

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            templateGenerator.generateYaml(unknownType);
        });

        assertEquals("Template introuvable pour le type : " + unknownType, exception.getMessage());
    }
}