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
    void deployTopologyShouldGenerateYamlAndCallDocker() throws IOException, InterruptedException {
        NodeDTO kaliNode = new NodeDTO("1", "kali", "MyKali");
        NodeDTO serverNode = new NodeDTO("2", "server", "MyServer");
        
        TopologyRequest request = new TopologyRequest(TEST_USER, List.of(kaliNode, serverNode), List.of());

        doNothing().when(builderService).executeDockerCompose(anyString(), anyString());

        String result = builderService.deployTopology(request);

        assertNotNull(result);
        assertTrue(result.startsWith("mykali_1_"));
    }

    @Test
    void generateDockerComposeYamlShouldCreateCorrectContent() {
        NodeDTO dbNode = new NodeDTO("3", "db", "Database");
        TopologyRequest request = new TopologyRequest(TEST_USER, List.of(dbNode), List.of());
        String deploymentId = "test-uuid";

        String yaml = builderService.generateDockerComposeYaml(request, deploymentId);

        assertTrue(yaml.contains("services:"));
        assertTrue(yaml.contains("database_3:"));
        assertTrue(yaml.contains("image: mysql:5.7"));
        assertTrue(yaml.contains("container_name: database_3_test-uuid"));
        assertTrue(yaml.contains("MYSQL_ROOT_PASSWORD: root"));
    }
    
    @Test
    void deployTopologyFallbackContainerName() throws IOException, InterruptedException {
        NodeDTO serverNode = new NodeDTO("1", "server", "Srv");
        TopologyRequest request = new TopologyRequest(TEST_USER, List.of(serverNode), List.of());
        
        doNothing().when(builderService).executeDockerCompose(anyString(), anyString());
        
        String result = builderService.deployTopology(request);
        assertTrue(result.startsWith("srv_1_"));
    }
}