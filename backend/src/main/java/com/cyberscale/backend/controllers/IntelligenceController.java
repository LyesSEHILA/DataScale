package com.cyberscale.backend.controllers;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import com.cyberscale.backend.models.DetectedThreat;
import com.cyberscale.backend.services.intelligence.LocalThreatIntelligenceService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceController {

    private final RabbitTemplate rabbitTemplate;
    // ON UTILISE LE SERVICE LOCAL ICI
    private final LocalThreatIntelligenceService threatIntelligenceService;

    public IntelligenceController(RabbitTemplate rabbitTemplate, LocalThreatIntelligenceService threatIntelligenceService) {
        this.rabbitTemplate = rabbitTemplate;
        this.threatIntelligenceService = threatIntelligenceService;
    }

    @PostMapping("/log")
    public ResponseEntity<Void> receiveLog(@RequestBody Map<String, String> logEntry) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_INTELLIGENCE,
                logEntry
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/analyze-ip")
    public ResponseEntity<DetectedThreat> analyzeIp(@RequestParam String ip) {
        DetectedThreat threat = threatIntelligenceService.analyzeAndSaveIp(ip);
        if (threat != null) {
            return ResponseEntity.ok(threat);
        }
        return ResponseEntity.internalServerError().build();
    }
}