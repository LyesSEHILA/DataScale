package com.cyberscale.backend.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.services.ContainerService;
import com.cyberscale.backend.services.rabbitmq.RabbitMQProducer;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/arena")
@CrossOrigin(origins = "*") 
public class ArenaController {

    private static final Logger logger = LoggerFactory.getLogger(ArenaController.class);

    private final ArenaService arenaService;
    private final ContainerService containerService;
    private final RabbitMQProducer rabbitMQProducer;

    public ArenaController(ArenaService arenaService, 
                           ContainerService containerService, 
                           RabbitMQProducer rabbitMQProducer) {
        this.arenaService = arenaService;
        this.containerService = containerService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public record FlagRequest(
        @JsonProperty("userId") Long userId,
        @JsonProperty("challengeId") String challengeId,
        @JsonProperty("flag") String flag,
        @JsonProperty("mode") String mode
    ) {}

    // 👇 C'EST ICI QUE CA BLOQUAIT : J'ai ajouté le champ 'mode'
    public record CommandRequest(
        @JsonProperty("userId") String userId,
        @JsonProperty("containerId") String containerId,
        @JsonProperty("command") String command,
        @JsonProperty("mode") String mode // <--- IL MANQUAIT CETTE LIGNE
    ) {}

    @MessageMapping("/arena") 
    @SendTo("/topic/arena")  
    public String handleInput(String message) {
        return message;
    }

    @PostMapping("/start/{challengeId}")
    public ResponseEntity<Map<String, String>> startArena(@PathVariable String challengeId) {
        try {
            String containerId = arenaService.startChallengeEnvironment(challengeId);
            return ResponseEntity.ok(Map.of("containerId", containerId));
        } catch (Exception e) {
            logger.error("Error starting arena for {}", challengeId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Unable to start challenge environment"));
        }
    }

    @PostMapping("/stop/{containerId}")
    public ResponseEntity<Void> stopArena(@PathVariable String containerId) {
        arenaService.stopChallengeEnvironment(containerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executeCommand(@RequestBody CommandRequest request) {
        try {
            // Note: executeCommand n'utilise pas le mode pour l'instant, c'est l'analyse qui s'en charge
            rabbitMQProducer.sendGameEvent(request.userId(), request.command(), request.containerId());
            String result = containerService.executeCommand(request.containerId(), request.command());
            return ResponseEntity.ok(Map.of("output", result));
        } catch (Exception e) {
            logger.error("Error executing command for user {}", request.userId(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Command execution failed"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateFlag(@RequestBody FlagRequest request) {
        boolean success = arenaService.validateFlag(request.userId(), request.challengeId(), request.flag());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Flag valide ! Points attribués.", "success", true));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Flag incorrect.", "success", false));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<Void> analyzeCommand(@RequestBody CommandRequest request) {
        String cleanCommand = request.command().trim();
        
        if (!cleanCommand.isEmpty()) {
            // On récupère le mode, ou "TUTORIAL" par défaut s'il est null
            String gameMode = (request.mode() != null) ? request.mode() : "TUTORIAL";
            
            // On concatène : "MODE|COMMANDE"
            String payload = gameMode + "|" + cleanCommand;
            
            rabbitMQProducer.sendGameEvent(
                request.userId(), 
                payload, 
                request.containerId()
            );
        }
        return ResponseEntity.ok().build();
    }
}