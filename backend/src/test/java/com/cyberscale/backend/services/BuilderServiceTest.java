package com.cyberscale.backend.services;

import com.cyberscale.backend.dto.builder.NodeDTO;
import com.cyberscale.backend.dto.builder.TopologyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
        // GIVEN: Un de chaque type pour couvrir tous les 'case' du switch
        NodeDTO kali = new NodeDTO("1", "kali", "Kali");
        NodeDTO server = new NodeDTO("2", "server", "Server");
        NodeDTO db = new NodeDTO("3", "db", "DB");
        NodeDTO other = new NodeDTO("4", "other", "Other"); // Default case

        TopologyRequest request = new TopologyRequest(TEST_USER, List.of(kali, server, db, other), List.of());

        // Mock de l'exécution Docker (on veut juste tester la génération et le flux)
        doNothing().when(builderService).executeDockerCompose(anyString(), anyString());

        // WHEN
        String result = builderService.deployTopology(request);

        // THEN
        assertNotNull(result);
        
        // On vérifie le YAML généré indirectement via une méthode publique ou protected si accessible
        // Ici on fait confiance à deployTopology qui appelle generateDockerComposeYaml
        // On peut vérifier le nom du conteneur de retour
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
        // Pas de Kali, doit retourner le premier noeud
        NodeDTO server = new NodeDTO("1", "server", "Srv");
        TopologyRequest req = new TopologyRequest(TEST_USER, List.of(server), List.of());
        
        doNothing().when(builderService).executeDockerCompose(anyString(), anyString());
        
        String result = builderService.deployTopology(req);
        assertTrue(result.startsWith("srv_1_"));
    }
}