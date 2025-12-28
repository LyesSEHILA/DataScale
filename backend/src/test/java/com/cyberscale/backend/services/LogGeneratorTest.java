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
        // WHEN
        List<String> logs = logGenerator.generateLogs();
        
        // THEN
        assertNotNull(logs);
        // Accès autorisé car package-private
        assertEquals(LogGenerator.TOTAL_LOGS, logs.size());
    }

    @Test
    void generateLogs_ShouldContainAnomalySequence() {
        // WHEN
        List<String> logs = logGenerator.generateLogs();

        // THEN
        long anomalyCount = logs.stream()
                .filter(line -> line.contains(LogGenerator.ATTACKER_IP))
                .filter(line -> line.contains(String.valueOf(LogGenerator.ANOMALY_STATUS)))
                .filter(line -> line.contains(LogGenerator.TARGET_URL))
                .count();

        assertEquals(LogGenerator.ANOMALY_LOGS, anomalyCount);
    }

    @Test
    void generateLogs_ShouldBeValidApacheFormat() {
        List<String> logs = logGenerator.generateLogs();
        String firstLog = logs.get(0);

        boolean matches = firstLog.matches("^\\d{1,3}(\\.\\d{1,3}){3} - - \\[.*\\] \".*\" \\d{3} \\d+$");
        
        assertTrue(matches);
        assertFalse(firstLog.isEmpty());
    }
}