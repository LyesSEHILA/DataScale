package com.cyberscale.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogGeneratorTest {

    private LogGenerator logGenerator;

    @BeforeEach
    void setUp() {
        logGenerator = new LogGenerator();
    }

    @Test
    void generateLogs_ShouldReturnCorrectNumberOfLogs() {
        List<String> logs = logGenerator.generateLogs();
        
        assertNotNull(logs);
        assertEquals(500, logs.size());
    }

    @Test
    void generateLogs_ShouldContainAnomalySequence() {
        List<String> logs = logGenerator.generateLogs();

        // On compte combien de lignes correspondent exactement au scénario d'attaque
        long anomalyCount = logs.stream()
                .filter(line -> line.contains("192.168.1.66")) // IP Attaquant
                .filter(line -> line.contains("404"))          // Code Erreur
                .filter(line -> line.contains("/admin/secret_config.php")) // Cible
                .count();

        assertEquals(50, anomalyCount);
    }

    @Test
    void generateLogs_ShouldBeValidApacheFormat() {
        List<String> logs = logGenerator.generateLogs();
        String firstLog = logs.get(0);

        // Regex basique pour vérifier la structure : IP - - [Date] "REQ" Status Size
        // Ex: 10.0.0.1 - - [10/Oct/2000:13:55:36 +0000] "GET /foo HTTP/1.1" 200 123
        boolean matches = firstLog.matches("^\\d{1,3}(\\.\\d{1,3}){3} - - \\[.*\\] \".*\" \\d{3} \\d+$");
        
        assertTrue(matches);
        assertFalse(firstLog.isEmpty());
    }
}