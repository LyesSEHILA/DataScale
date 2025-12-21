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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.config.SecurityConfig;
import com.cyberscale.backend.controllers.ArenaController.FlagRequest; 
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=")
@Import(SecurityConfig.class)
class ArenaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ChallengeRepository challengeRepository;
    @MockitoBean private ArenaService arenaService;

    @Test
    void testValidateFlag_Integration() throws Exception {
        // 1. Setup Données
        // On sauvegarde l'user pour avoir un ID valide généré par la BDD
        User user = userRepository.saveAndFlush(new User("Tester", "test@arena.com", "pass"));
        // (Le challenge en base est inutile pour le Mock, mais on peut le laisser)
        challengeRepository.saveAndFlush(new Challenge("TEST_CTF", "Test", "Desc", "FLAG123", 50));

        // --- CORRECTION ICI ---
        // On "programme" le Mock pour qu'il dise OUI quand on lui envoie les bonnes infos
        when(arenaService.validateFlag(user.getId(), "TEST_CTF", "FLAG123")).thenReturn(true);
        // ----------------------

        // --- CAS 1 : BON FLAG ---
        String jsonRequest = String.format("{\"userId\": %d, \"challengeId\": \"TEST_CTF\", \"flag\": \"FLAG123\"}", user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
                
        // --- CAS 2 : MAUVAIS FLAG ---
        User user2 = userRepository.saveAndFlush(new User("Loser", "loser@arena.com", "pass"));

        // Pas besoin de stubber le mock ici : par défaut il renvoie false, ce qui est parfait pour le test d'échec
        String badRequest = String.format("{\"userId\": %d, \"challengeId\": \"TEST_CTF\", \"flag\": \"WRONG\"}", user2.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void startArena_ShouldReturnContainerId() throws Exception {
        // Quand on appelle le service, il renvoie un ID fictif
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

    @Test
    void validateFlag_ShouldReturnSuccess() throws Exception {
        // 1. Setup du Mock : Le service doit dire "Oui c'est bon" (true)
        when(arenaService.validateFlag(eq(1L), eq("CTF_1"), eq("FLAG{123}"))).thenReturn(true);

        // 2. Création du corps de la requête JSON
        String jsonRequest = "{\"userId\":1, \"challengeId\":\"CTF_1\", \"flag\":\"FLAG{123}\"}";

        // 3. Appel & Vérif
        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()) // On attend 200 OK
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void validateFlag_ShouldReturn400_WhenWrong() throws Exception {
        // Cas d'échec
        when(arenaService.validateFlag(any(), any(), any())).thenReturn(false);

        String jsonRequest = "{\"userId\":1, \"challengeId\":\"CTF_1\", \"flag\":\"WRONG\"}";

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()) // On attend 400
                .andExpect(jsonPath("$.success").value(false));
    }
}