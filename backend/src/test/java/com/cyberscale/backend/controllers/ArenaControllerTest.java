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
    
    @MockitoBean private ArenaService arenaService;
    @MockitoBean private ContainerService containerService;
    @MockitoBean private RabbitMQProducer rabbitMQProducer;

    // --- TEST 1 : Validate Flag (Succès) ---
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

    // --- TEST 2 : Validate Flag (Échec - Nouveau) ---
    @Test
    void validateFlag_Failure() throws Exception {
        User user = userRepository.save(new User("TesterFail", "fail@arena.com", "pass"));
        when(arenaService.validateFlag(anyLong(), anyString(), anyString())).thenReturn(false);

        String jsonRequest = String.format("{\"userId\": %d, \"challengeId\": \"C1\", \"flag\": \"BadFlag\"}", user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()) // Vérifie le status 400
                .andExpect(jsonPath("$.success").value(false));
    }

    // --- TEST 3 : Start Arena (Succès) ---
    @Test
    void startArena_Success() throws Exception {
        when(arenaService.startChallengeEnvironment("C1")).thenReturn("docker-id");

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerId").value("docker-id"));
    }

    // --- TEST 4 : Start Arena (Erreur Technique - Nouveau) ---
    @Test
    void startArena_Exception() throws Exception {
        // On simule une erreur qui déclenche le catch du controller
        when(arenaService.startChallengeEnvironment("C1")).thenThrow(new RuntimeException("Docker HS"));

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isInternalServerError()) // 500
                .andExpect(jsonPath("$.error").exists());
    }

    // --- TEST 5 : Stop Arena (Nouveau) ---
    @Test
    void stopArena_Success() throws Exception {
        doNothing().when(arenaService).stopChallengeEnvironment("c1");

        mockMvc.perform(post("/api/arena/stop/c1"))
                .andExpect(status().isOk());
        
        verify(arenaService).stopChallengeEnvironment("c1");
    }

    // --- TEST 6 : Execute Command (Succès) ---
    @Test
    void executeCommand_Success() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\"}";
        when(containerService.executeCommand("c1", "ls")).thenReturn("file1.txt");

        mockMvc.perform(post("/api/arena/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value("file1.txt"));

        verify(rabbitMQProducer).sendGameEvent("u1", "ls", "c1");
    }

    // --- TEST 7 : Execute Command (Erreur Technique - Nouveau) ---
    @Test
    void executeCommand_Exception() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\"}";
        
        // On fait planter RabbitMQ ou Docker pour déclencher le catch
        doThrow(new RuntimeException("Rabbit Down")).when(rabbitMQProducer).sendGameEvent(any(), any(), any());

        mockMvc.perform(post("/api/arena/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError()) // 500
                .andExpect(jsonPath("$.error").value("Command execution failed"));
    }
}