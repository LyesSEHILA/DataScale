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
import com.cyberscale.backend.config.SecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class ArenaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    
    @MockitoBean private ArenaService arenaService;

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
    void validateFlag_Failure() throws Exception {
        when(arenaService.validateFlag(any(), any(), any())).thenReturn(false);
        String jsonRequest = "{\"userId\": 1, \"challengeId\": \"C1\", \"flag\": \"WRONG\"}";

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startArena_Success() throws Exception {
        when(arenaService.startChallengeEnvironment("C1")).thenReturn("docker-id");

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerId").value("docker-id"));
    }

    @Test
    void startArena_Error() throws Exception {
        // Teste le try/catch du controller
        when(arenaService.startChallengeEnvironment("C1")).thenThrow(new RuntimeException("Docker HS"));

        mockMvc.perform(post("/api/arena/start/C1"))
                .andExpect(status().isInternalServerError()) // 500
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void stopArena_Success() throws Exception {
        mockMvc.perform(post("/api/arena/stop/123"))
                .andExpect(status().isOk());
        verify(arenaService).stopChallengeEnvironment("123");
    }
}