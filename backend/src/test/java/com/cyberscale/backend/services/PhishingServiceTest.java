package com.cyberscale.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PhishingServiceTest {

    private PhishingService phishingService;

    @BeforeEach
    void setUp() {
        phishingService = new PhishingService();
    }

    // --- Tests de analyzeClick ---

    @Test
    void analyzeClick_ShouldReturnTrue_WhenTrapIsClicked() {
        // Test avec un vrai piège du SCENARIO_1 (ex: sender-email)
        Map<String, Object> result = phishingService.analyzeClick("SCENARIO_1", "sender-email");

        assertNotNull(result);
        assertEquals(true, result.get("isTrap"));
        assertTrue(result.get("message").toString().contains("Bien vu"));
    }

    @Test
    void analyzeClick_ShouldReturnFalse_WhenSafeElementIsClicked() {
        // Test avec un élément qui n'est PAS un piège
        Map<String, Object> result = phishingService.analyzeClick("SCENARIO_1", "safe-text-element");

        assertNotNull(result);
        assertEquals(false, result.get("isTrap"));
        assertTrue(result.get("message").toString().contains("légitime"));
    }

    @Test
    void analyzeClick_ShouldReturnFalse_WhenScenarioDoesNotExist() {
        // Test avec un ID de scénario inconnu
        Map<String, Object> result = phishingService.analyzeClick("SCENARIO_999", "sender-email");

        // Doit être considéré comme "pas un piège" car le scénario n'existe pas
        assertEquals(false, result.get("isTrap"));
    }

    @Test
    void analyzeClick_ShouldHandleNullInputs() {
        Map<String, Object> r1 = phishingService.analyzeClick(null, "sender-email");
        Map<String, Object> r2 = phishingService.analyzeClick("SCENARIO_1", null);

        assertEquals("error", r1.get("status"));
        assertEquals("error", r2.get("status"));
    }

    // --- Tests de getScenarioInfo ---

    @Test
    void getScenarioInfo_ShouldReturnData_WhenScenarioExists() {
        Map<String, Object> info = phishingService.getScenarioInfo("SCENARIO_1");

        assertNotNull(info);
        // On sait qu'il y a 4 pièges dans le scénario 1
        assertEquals(4, info.get("totalTraps"));
        assertTrue(info.get("lesson").toString().contains("Typosquatting"));
    }

    @Test
    void getScenarioInfo_ShouldReturnNull_WhenScenarioUnknown() {
        Map<String, Object> info = phishingService.getScenarioInfo("SCENARIO_UNKNOWN");
        assertNull(info);
    }
}