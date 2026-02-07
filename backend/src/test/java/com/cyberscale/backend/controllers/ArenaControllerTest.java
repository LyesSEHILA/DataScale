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

        // 👇 AJOUT DE "mode" DANS LE JSON
        String jsonRequest = String.format(
            "{\"userId\": %d, \"challengeId\": \"C1\", \"flag\": \"F\", \"mode\": \"TUTORIAL\"}", 
            user.getId()
        );

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // --- TEST 2 : Validate Flag (Échec) ---
    @Test
    void validateFlag_Failure() throws Exception {
        User user = userRepository.save(new User("TesterFail", "fail@arena.com", "pass"));
        when(arenaService.validateFlag(anyLong(), anyString(), anyString())).thenReturn(false);

        // 👇 AJOUT DE "mode" DANS LE JSON
        String jsonRequest = String.format(
            "{\"userId\": %d, \"challengeId\": \"C1\", \"flag\": \"BadFlag\", \"mode\": \"TUTORIAL\"}", 
            user.getId()
        );

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
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

    // --- TEST 4 : Start Arena (Erreur Technique) ---
    @Test
    void startArena_Exception() throws Exception {
        when(arenaService.startChallengeEnvironment("C1")).thenThrow(new RuntimeException("Docker HS"));

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- TEST 5 : Stop Arena (Succès) ---
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
        // 👇 AJOUT DE "mode" DANS LE JSON
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\", \"mode\": \"TUTORIAL\"}";
        
        when(containerService.executeCommand("c1", "ls")).thenReturn("file1.txt");

        mockMvc.perform(post("/api/arena/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value("file1.txt"));

        // Vérifie que RabbitMQ est bien appelé
        verify(rabbitMQProducer).sendGameEvent("u1", "ls", "c1");
    }

    // --- TEST 7 : Execute Command (Erreur Technique) ---
    @Test
    void executeCommand_Exception() throws Exception {
        // 👇 AJOUT DE "mode" DANS LE JSON
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\", \"mode\": \"TUTORIAL\"}";
        
        doThrow(new RuntimeException("Rabbit Down")).when(rabbitMQProducer).sendGameEvent(any(), any(), any());

        mockMvc.perform(post("/api/arena/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Command execution failed"));
    }

    @Test
    void analyzeCommand_ShouldSendEventToRabbit() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\", \"mode\": \"RED_TEAM\"}";

        // On n'attend pas de retour body, juste un statut OK
        mockMvc.perform(post("/api/arena/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());

        // Vérification Clé : Le contrôleur a bien concaténé "MODE|COMMANDE"
        verify(rabbitMQProducer).sendGameEvent("u1", "RED_TEAM|ls", "c1");
    }
    
    @Test
    void analyzeCommand_ShouldDefaultToTutorial_WhenModeIsNull() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\"}"; // Pas de mode

        mockMvc.perform(post("/api/arena/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());

        // Vérification du défaut
        verify(rabbitMQProducer).sendGameEvent("u1", "TUTORIAL|ls", "c1");
    }
    
}