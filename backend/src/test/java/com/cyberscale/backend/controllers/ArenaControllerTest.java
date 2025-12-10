package com.cyberscale.backend.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class ArenaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ChallengeRepository challengeRepository;

    @Test
    void testValidateFlag_Integration() throws Exception {
        // 1. Setup Données
        User user = userRepository.saveAndFlush(new User("Tester", "test@arena.com", "pass"));
        challengeRepository.saveAndFlush(new Challenge("TEST_CTF", "Test", "Desc", "FLAG123", 50));

        // --- CAS 1 : BON FLAG ---
        String jsonRequest = String.format("{\"userId\": %d, \"challengeId\": \"TEST_CTF\", \"flag\": \"FLAG123\"}", user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
                
        // --- CAS 2 : MAUVAIS FLAG (Avec un NOUVEL utilisateur) ---
        // On crée un user vierge pour être sûr qu'il n'a pas déjà validé le challenge
        User user2 = userRepository.saveAndFlush(new User("Loser", "loser@arena.com", "pass"));

        String badRequest = String.format("{\"userId\": %d, \"challengeId\": \"TEST_CTF\", \"flag\": \"WRONG\"}", user2.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badRequest))
                .andExpect(status().isBadRequest()) // Maintenant ça sera bien 400 car user2 n'a pas encore gagné
                .andExpect(jsonPath("$.success").value(false));
    }
}