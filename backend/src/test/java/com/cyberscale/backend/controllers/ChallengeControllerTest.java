package com.cyberscale.backend.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.services.LogGenerator;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class ChallengeControllerTest {

    @Autowired private MockMvc mockMvc;
    
    @MockitoBean
    private ArenaService arenaService;

    @MockitoBean 
    private LogGenerator logGenerator;

    // --- CORRECTION SONARCLOUD ICI ---
    // On définit les IPs en constantes et on supprime l'alerte de sécurité
    
    @SuppressWarnings("java:S1313")
    private static final String TEST_IP = "192.168.1.1";

    @SuppressWarnings("java:S1313")
    private static final String ATTACKER_IP = "192.0.2.66";

    @SuppressWarnings("java:S1313")
    private static final String WRONG_IP = "10.10.10.10";
    // ---------------------------------

    @Test
    void testGetAllChallenges() throws Exception {
        ChallengeDTO c1 = new ChallengeDTO("C1", "Intro", "Desc", 10, "FACILE", true);
        ChallengeDTO c2 = new ChallengeDTO("C2", "Expert", "Desc", 100, "HARDCORE", false);
        
        given(arenaService.getAllChallenges(1L)).willReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/challenges?userId=1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("C1")))
                .andExpect(jsonPath("$[0].isValidated", is(true)))
                .andExpect(jsonPath("$[1].difficulty", is("HARDCORE")));
    }

    @Test
    void testGetOneChallenge() throws Exception {
        ChallengeDTO c1 = new ChallengeDTO("C1", "Intro", "Desc", 10, "N/A", false);
        given(arenaService.getChallengeById("C1")).willReturn(c1);

        mockMvc.perform(get("/api/challenges/C1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Intro")));
    }

    @Test
    void testGetChallengeLogs() throws Exception {
        // Utilisation des constantes pour construire les logs
        List<String> mockLogs = List.of(
            TEST_IP + " - - [GET /] 200", 
            ATTACKER_IP + " - - [GET /admin] 404"
        );
        given(logGenerator.generateLogs()).willReturn(mockLogs);

        mockMvc.perform(get("/api/challenges/logs")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                // Vérification dynamique avec la constante
                .andExpect(jsonPath("$[0]", containsString(TEST_IP)));
    }

    @Test
    void testValidateAttackerIp_Success() throws Exception {
        given(logGenerator.getAttackerIp()).willReturn(ATTACKER_IP);
        
        // Construction propre du JSON avec l'IP
        String jsonBody = String.format("{\"ip\": \"%s\"}", ATTACKER_IP);

        mockMvc.perform(post("/api/challenges/logs/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Bravo")));
    }

    @Test
    void testValidateAttackerIp_Failure() throws Exception {
        given(logGenerator.getAttackerIp()).willReturn(ATTACKER_IP);
        
        // On utilise une mauvaise IP (constante) pour tester l'échec
        String jsonBody = String.format("{\"ip\": \"%s\"}", WRONG_IP);

        mockMvc.perform(post("/api/challenges/logs/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest()) // On attend une erreur 400
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Ce n'est pas l'IP suspecte")));
    }
}