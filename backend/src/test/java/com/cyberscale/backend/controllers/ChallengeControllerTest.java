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
        List<String> mockLogs = List.of(
            "192.168.1.1 - - [GET /] 200", 
            "192.0.2.66 - - [GET /admin] 404"
        );
        given(logGenerator.generateLogs()).willReturn(mockLogs);
        mockMvc.perform(get("/api/challenges/logs")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", containsString("192.168.1.1")));
    }

    @Test
    void testValidateAttackerIp_Success() throws Exception {
        given(logGenerator.getAttackerIp()).willReturn("192.0.2.66");
        String jsonBody = "{\"ip\": \"192.0.2.66\"}";

        mockMvc.perform(post("/api/challenges/logs/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Bravo")));
    }

    @Test
    void testValidateAttackerIp_Failure() throws Exception {
        given(logGenerator.getAttackerIp()).willReturn("192.0.2.66");
        String jsonBody = "{\"ip\": \"10.10.10.10\"}";

        mockMvc.perform(post("/api/challenges/logs/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest()) // On attend une erreur 400
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Ce n'est pas l'IP suspecte")));
    }
}