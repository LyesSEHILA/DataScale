package com.cyberscale.backend.controllers;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.UserRepository;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.services.ContainerService;
import com.cyberscale.backend.services.rabbitmq.RabbitMQProducer;

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
    void validateFlagSuccess() throws Exception {
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
    void validateFlagFailure() throws Exception {
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
    void startArenaSuccess() throws Exception {
        when(arenaService.startChallengeEnvironment(anyLong(), eq("C1"))).thenReturn("docker-id");

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerId").value("docker-id"));
    }

    @Test
    void startArenaException() throws Exception {
        when(arenaService.startChallengeEnvironment(anyLong(), eq("C1")))
            .thenThrow(new RuntimeException("Docker HS"));

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Docker HS"));
    }

    @Test
    void stopArenaSuccess() throws Exception {
        doNothing().when(arenaService).stopChallengeEnvironment("c1");

        mockMvc.perform(post("/api/arena/stop/c1"))
                .andExpect(status().isOk());
        
        verify(arenaService).stopChallengeEnvironment("c1");
    }

    @Test
    void executeCommandSuccess() throws Exception {
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
    void executeCommandException() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\", \"mode\": \"TUTORIAL\"}";
        doThrow(new RuntimeException("Rabbit Down")).when(rabbitMQProducer).sendGameEvent(any(), any(), any());

        mockMvc.perform(post("/api/arena/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Command execution failed"));
    }

    @Test
    void analyzeCommandShouldSendEventToRabbit() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\", \"mode\": \"RED_TEAM\"}";
        mockMvc.perform(post("/api/arena/analyze").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
                .andExpect(status().isOk());
        verify(rabbitMQProducer).sendGameEvent("u1", "RED_TEAM|ls", "c1");
    }
    
    @Test
    void analyzeCommandShouldDefaultToTutorialWhenModeIsNull() throws Exception {
        String jsonRequest = "{\"userId\": \"u1\", \"containerId\": \"c1\", \"command\": \"ls\"}"; 
        mockMvc.perform(post("/api/arena/analyze").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
                .andExpect(status().isOk());
        verify(rabbitMQProducer).sendGameEvent("u1", "TUTORIAL|ls", "c1");
    }
}