package com.cyberscale.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LogGeneratorTest {

    private LogGenerator logGenerator;

    @BeforeEach
    void setUp() {
        logGenerator = new LogGenerator();
    }

    @Test
    void generateLogs_ShouldGenerateLogsWithAnomaly() {
        List<String> logs = logGenerator.generateLogs();

        assertNotNull(logs);
        
        // Vérifier le nombre total de logs (450 normaux + 50 anormaux)
        assertEquals(500, logs.size());

        // Vérifier la présence de l'anomalie
        long anomalyCount = logs.stream()
                .filter(line -> line.contains("192.168.1.66")) // L'IP de l'attaquant
                .filter(line -> line.contains("404"))          // Le code d'erreur
                .filter(line -> line.contains("/admin/secret_config.php")) // L'URL visée
                .count();

        assertEquals(50, anomalyCount);

        // Vérifier que la liste n'est pas vide et contient des données lisibles
        assertFalse(logs.get(0).isEmpty());
        
    }
}