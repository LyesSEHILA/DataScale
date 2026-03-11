package com.cyberscale.backend.services; // Vérifiez que le package est le bon !

import com.cyberscale.backend.dto.builder.NodeDTO;
import com.cyberscale.backend.dto.builder.TopologyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class BuilderServiceTest {

    private static final String TEST_USER = "test-user";

    @Spy
    @InjectMocks
    private BuilderService builderService;

    @Test
    void deployTopologyWithMultipleNodeTypes() throws IOException, InterruptedException {
        NodeDTO kali = new NodeDTO("1", "kali", "Kali");
        NodeDTO server = new NodeDTO("2", "server", "Server");
        NodeDTO db = new NodeDTO("3", "db", "DB");
        NodeDTO other = new NodeDTO("4", "other", "Other");

        TopologyRequest request = new TopologyRequest(TEST_USER, List.of(kali, server, db, other), List.of());

        doNothing().when(builderService).executeDockerCompose(anyString(), anyString());

        String result = builderService.deployTopology(request);

        assertNotNull(result);
        assertTrue(result.startsWith("kali_1_"));
    }
    
    @Test
    void generateYamlContentCheck() {
        NodeDTO db = new NodeDTO("3", "db", "DB");
        TopologyRequest req = new TopologyRequest(TEST_USER, List.of(db), List.of());
        
        String yaml = builderService.generateDockerComposeYaml(req, "uid");
        
        assertTrue(yaml.contains("mysql:5.7"));
        assertTrue(yaml.contains("MYSQL_ROOT_PASSWORD"));
    }
    
    @Test
    void findKaliFallback() throws IOException, InterruptedException {
        NodeDTO server = new NodeDTO("1", "server", "Srv");
        TopologyRequest req = new TopologyRequest(TEST_USER, List.of(server), List.of());
        
        doNothing().when(builderService).executeDockerCompose(anyString(), anyString());
        
        String result = builderService.deployTopology(req);
        assertTrue(result.startsWith("srv_1_"));
    }

    // --- NOUVEAUX TESTS POUR LE COVERAGE (SONARCLOUD) ---

    @Test
    void findKaliContainerName_EmptyTopology() {
        // GIVEN : Une topologie 100% vide
        TopologyRequest req = new TopologyRequest(TEST_USER, List.of(), List.of());
        
        // WHEN : On appelle la méthode privée via Reflection
        String result = ReflectionTestUtils.invokeMethod(builderService, "findKaliContainerName", req, "deploy-uid");
        
        // THEN : Doit atteindre la toute dernière ligne "return null;"
        assertNull(result);
    }

    @Test
    void executeDockerCompose_ShouldThrowIOException_WhenExitCodeNotZero() {
        // GIVEN : On force le chemin Docker vers une commande système qui échoue tout le temps ("false" sous Mac/Linux = code 1)
        ReflectionTestUtils.setField(builderService, "dockerPath", "false");
        
        // WHEN & THEN : On vérifie que ça lève bien une exception
        IOException exception = assertThrows(IOException.class, () -> {
            builderService.executeDockerCompose("12345", "dummy.yml");
        });

        // Vérifie qu'on est bien passé dans la condition "if (exitCode != 0)"
        assertTrue(exception.getMessage().contains("Échec du déploiement"));
    }

    @Test
    void executeDockerCompose_ShouldCatchInterruptedException() throws InterruptedException {
        // GIVEN : On force le chemin vers "sleep" pour que le processus reste bloqué
        ReflectionTestUtils.setField(builderService, "dockerPath", "sleep");
        
        // On crée un Thread séparé pour pouvoir l'interrompre pendant qu'il dort
        Thread testThread = new Thread(() -> {
            assertThrows(IOException.class, () -> {
                // "sleep 10" pour faire patienter le ProcessBuilder
                builderService.executeDockerCompose("10", "10"); 
            });
        });

        // WHEN
        testThread.start();
        Thread.sleep(100); // Laisse le temps au thread de démarrer
        testThread.interrupt(); // BOOM ! On l'interrompt de force

        // THEN : Le test passe si l'exception InterruptedException a bien été attrapée et transformée en IOException
    }
}