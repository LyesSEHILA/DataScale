package com.cyberscale.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows; // ✅ Import Ajouté
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import jakarta.servlet.ServletException; // ✅ Import Ajouté

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
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class ArenaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    
    @MockitoBean private ArenaService arenaService;
    @MockitoBean private ContainerService containerService;
    @MockitoBean private RabbitMQProducer rabbitMQProducer;

    @Test
    void validateFlag_Success() throws Exception {
        User user = userRepository.save(new User("Tester", "test@arena.com", "pass"));
        when(arenaService.validateFlag(anyLong(), anyString(), anyString())).thenReturn(true);
        
        String jsonRequest = String.format("{\"userId\": %d, \"challengeId\": \"C1\", \"flag\": \"F\", \"mode\": \"TUTORIAL\"}", user.getId());
        
        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void validateFlag_Failure() throws Exception {
        User user = userRepository.save(new User("TesterFail", "fail@arena.com", "pass"));
        when(arenaService.validateFlag(anyLong(), anyString(), anyString())).thenReturn(false);
        
        String jsonRequest = String.format("{\"userId\": %d, \"challengeId\": \"C1\", \"flag\": \"BadFlag\", \"mode\": \"TUTORIAL\"}", user.getId());
        
        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void startArena_Success() throws Exception {
        when(arenaService.startChallengeEnvironment(anyLong(), eq("C1"))).thenReturn("docker-id");

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerId").value("docker-id"));
    }

    // 🚨 CORRECTION MAJEURE ICI
    @Test
    void startArena_Exception() {
        // GIVEN
        when(arenaService.startChallengeEnvironment(anyLong(), eq("C1")))
            .thenThrow(new RuntimeException("Docker HS"));

        // WHEN & THEN
        // Puisque le contrôleur ne capture pas l'exception, elle remonte comme une ServletException.
        // On vérifie que c'est bien ce qui se passe.
        ServletException exception = assertThrows(ServletException.class, () -> {
            mockMvc.perform(post("/api/arena/start/C1"));
        });

        // On vérifie que la cause est bien notre RuntimeException
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertTrue(exception.getCause().getMessage().contains("Docker HS"));
    }

    @Test
    void stopArena_Success() throws Exception {
        doNothing().when(arenaService).stopChallengeEnvironment("c1");

        mockMvc.perform(post("/api/arena/stop/c1"))
                .andExpect(status().isOk());
        
        verify(arenaService).stopChallengeEnvironment("c1");
    }

    @Test
    void executeCommand_Success() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\", \"mode\": \"TUTORIAL\"}";
        when(containerService.executeCommand("c1", "ls")).thenReturn("file1.txt");

        mockMvc.perform(post("/api/arena/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value("file1.txt"));

        verify(rabbitMQProducer).sendGameEvent("u1", "ls", "c1");
    }

    @Test
    void executeCommand_Exception() throws Exception {
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
        mockMvc.perform(post("/api/arena/analyze").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
                .andExpect(status().isOk());
        verify(rabbitMQProducer).sendGameEvent("u1", "RED_TEAM|ls", "c1");
    }
    
    @Test
    void analyzeCommand_ShouldDefaultToTutorial_WhenModeIsNull() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\"}"; 
        mockMvc.perform(post("/api/arena/analyze").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
                .andExpect(status().isOk());
        verify(rabbitMQProducer).sendGameEvent("u1", "TUTORIAL|ls", "c1");
    }
}