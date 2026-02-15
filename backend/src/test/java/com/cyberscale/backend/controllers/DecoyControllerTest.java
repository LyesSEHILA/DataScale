package com.cyberscale.backend.controllers;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import com.cyberscale.backend.dto.DeployDecoyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DecoyController.class)
class DecoyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate; // On simule RabbitMQ

    @Test
    @WithMockUser // Simule un utilisateur connecté
    void deployDecoyShouldSendRabbitMessage() throws Exception {
        DeployDecoyRequest request = new DeployDecoyRequest("user123", "mysql");

        mockMvc.perform(post("/api/decoy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .with(csrf())) // Important pour la sécurité Spring
                .andExpect(status().isAccepted()); // Vérifie le code 202

        // Vérifie que le message a bien été envoyé à RabbitMQ
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq("infra.deploy"),
                eq(request)
        );
    }
}