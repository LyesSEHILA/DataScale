package com.cyberscale.backend.controllers;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cyberscale.backend.config.SecurityConfig;
import com.cyberscale.backend.dto.builder.NodeDTO;
import com.cyberscale.backend.dto.builder.TopologyRequest;
import com.cyberscale.backend.services.BuilderService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class BuilderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private BuilderService builderService;

    @Test
    void deployNetwork_Success() throws Exception {
        // Correction : Suppression des coordonnées et ajout de l'userId
        TopologyRequest request = new TopologyRequest(
            "user1",
            List.of(new NodeDTO("1", "kali", "K")), 
            List.of()
        );

        when(builderService.deployTopology(any())).thenReturn("kali_container_id");

        mockMvc.perform(post("/api/builder/deploy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.deploymentId").value("kali_container_id"));
    }

    @Test
    void deployNetwork_Failure() throws Exception {
        // Correction : Ajout de l'userId
        TopologyRequest request = new TopologyRequest("user1", List.of(), List.of());

        when(builderService.deployTopology(any())).thenThrow(new RuntimeException("Deploy Error"));

        mockMvc.perform(post("/api/builder/deploy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Deploy Error"));
    }
}