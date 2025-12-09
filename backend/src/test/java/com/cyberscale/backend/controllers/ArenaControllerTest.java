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
        User user = userRepository.save(new User("Tester", "test@arena.com", "pass"));
        challengeRepository.save(new Challenge("TEST_CTF", "Test", "FLAG123", 50));

        // 2. Appel API avec le BON flag
        String jsonRequest = """
            {
                "userId": %d,
                "challengeId": "TEST_CTF",
                "flag": "FLAG123"
            }
        """.formatted(user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
                
        // 3. Appel API avec le MAUVAIS flag
        String badRequest = """
            {
                "userId": %d,
                "challengeId": "TEST_CTF",
                "flag": "WRONG"
            }
        """.formatted(user.getId());

        mockMvc.perform(post("/api/arena/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badRequest))
                .andExpect(status().isBadRequest()) // Ou 400
                .andExpect(jsonPath("$.success").value(false));
    }
}