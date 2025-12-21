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
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 3.4+
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.config.SecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=")
@Import(SecurityConfig.class)
class ArenaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ChallengeRepository challengeRepository;
    
    @MockitoBean // Remplace @MockBean dans Spring Boot 3.4
    private ArenaService arenaService;

    @Test
    void testValidateFlag_Success() throws Exception {
        // 1. Setup Données
        User user = userRepository.saveAndFlush(new User("Tester", "test@arena.com", "pass"));
        
        // 2. Mock du service
        when(arenaService.validateFlag(eq(user.getId()), eq("TEST_CTF"), eq("FLAG123"))).thenReturn(true);

        // 3. Appel API
        String jsonRequest = String.format("{\"userId\": %d, \"challengeId\": \"TEST_CTF\", \"flag\": \"FLAG123\"}", user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testValidateFlag_Failure() throws Exception {
        // 1. Setup Données
        User user = userRepository.saveAndFlush(new User("Loser", "loser@arena.com", "pass"));
        
        // 2. Mock du service (renvoie false par défaut, ou explicite)
        when(arenaService.validateFlag(any(), any(), any())).thenReturn(false);

        // 3. Appel API
        String badRequest = String.format("{\"userId\": %d, \"challengeId\": \"TEST_CTF\", \"flag\": \"WRONG\"}", user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void startArena_ShouldReturnContainerId() throws Exception {
        when(arenaService.startChallengeEnvironment("CTF_1")).thenReturn("docker-id-123");

        mockMvc.perform(post("/api/arena/start/CTF_1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerId").value("docker-id-123"));
    }

    @Test
    void stopArena_ShouldReturnOk() throws Exception {
        doNothing().when(arenaService).stopChallengeEnvironment(anyString());

        mockMvc.perform(post("/api/arena/stop/docker-id-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}