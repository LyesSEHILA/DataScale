package com.cyberscale.backend.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.UserRepository;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.services.ContainerService;
import com.cyberscale.backend.services.rabbitmq.RabbitMQProducer;
import com.cyberscale.backend.config.SecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécu pour les tests
@Import(SecurityConfig.class)
class ArenaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    
    // On mocke TOUS les services utilisés par le contrôleur
    @MockitoBean private ArenaService arenaService;
    @MockitoBean private ContainerService containerService; // Nouveau
    @MockitoBean private RabbitMQProducer rabbitMQProducer; // Nouveau

    @Test
    void validateFlag_Success() throws Exception {
        User user = userRepository.save(new User("Tester", "test@arena.com", "pass"));
        when(arenaService.validateFlag(anyLong(), anyString(), anyString())).thenReturn(true);

        String jsonRequest = String.format("{\"userId\": %d, \"challengeId\": \"C1\", \"flag\": \"F\"}", user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void startArena_Success() throws Exception {
        when(arenaService.startChallengeEnvironment("C1")).thenReturn("docker-id");

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerId").value("docker-id"));
    }

    // --- NOUVEAU TEST POUR LE TICKET W-02 ---
    @Test
    void executeCommand_Success() throws Exception {
        // GIVEN
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\"}";
        when(containerService.executeCommand("c1", "ls")).thenReturn("file1.txt");

        // WHEN
        mockMvc.perform(post("/api/arena/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value("file1.txt"));

        // Vérifie que RabbitMQ a bien été appelé ! (C'est le but du ticket)
        verify(rabbitMQProducer).sendGameEvent("u1", "ls", "c1");
    }
}