package com.cyberscale.backend.controllers;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceController {

    private final RabbitTemplate rabbitTemplate;

    public IntelligenceController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/log")
    public ResponseEntity<Void> receiveLog(@RequestBody Map<String, String> logEntry) {
        // Pousse le log reçu directement dans RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_INTELLIGENCE,
                logEntry
        );
        return ResponseEntity.ok().build();
    }
}
