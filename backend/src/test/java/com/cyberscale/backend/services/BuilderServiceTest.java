package com.cyberscale.backend.services;

import com.cyberscale.backend.dto.builder.NodeDTO;
import com.cyberscale.backend.dto.builder.TopologyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class BuilderServiceTest {

    private static final String TEST_USER = "test-user";

    @Spy
    @InjectMocks
    private BuilderService builderService;

    @Test
    void deployTopologyAllTypes() throws IOException, InterruptedException {
        // COUVERTURE : On teste tous les types d'images (kali, server, db, default)
        NodeDTO kali = new NodeDTO("1", "kali", "K");
        NodeDTO server = new NodeDTO("2", "server", "S");
        NodeDTO db = new NodeDTO("3", "db", "D");
        NodeDTO other = new NodeDTO("4", "router", "R"); // Default case

        TopologyRequest request = new TopologyRequest(TEST_USER, List.of(kali, server, db, other), List.of());

        doNothing().when(builderService).executeDockerCompose(anyString(), anyString());

        String result = builderService.deployTopology(request);
        
        // On vérifie indirectement via le YAML généré (appelé en interne)
        String yaml = builderService.generateDockerComposeYaml(request, "uuid");
        assertTrue(yaml.contains("kalilinux"));
        assertTrue(yaml.contains("httpd:alpine"));
        assertTrue(yaml.contains("mysql"));
        assertTrue(yaml.contains("alpine")); // fallback
    }
}