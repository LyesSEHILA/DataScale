package com.cyberscale.backend.controllers;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.services.ContainerService; // Assure-toi que ce service existe
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import com.cyberscale.backend.services.rabbitmq.RabbitMQProducer;

/**
 * Controleur REST et WebSocket gérant l'arene de jeu.
 * Responsabilités :
 * - REST : Démarrer/Arrêter les conteneurs Docker.
 * - REST : Exécuter les commandes Shell (et notifier RabbitMQ).
 * - REST : Valider les Flags.
 * - WS : Chat/Echo.
 */
@RestController
@RequestMapping("/api/arena")
@CrossOrigin(origins = "*") 
public class ArenaController {

    private final ArenaService arenaService;
    private final ContainerService containerService; // Nécessaire pour exécuter les commandes
    private final RabbitMQProducer rabbitMQProducer; // Nécessaire pour le Ticket W-02

    // Injection par constructeur (Plus propre que @Autowired sur les champs)
    public ArenaController(ArenaService arenaService, 
                           ContainerService containerService, 
                           RabbitMQProducer rabbitMQProducer) {
        this.arenaService = arenaService;
        this.containerService = containerService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    // --- DTOs ---
    public record FlagRequest(
        @JsonProperty("userId") Long userId,
        @JsonProperty("challengeId") String challengeId,
        @JsonProperty("flag") String flag
    ) {}

    public record CommandRequest(
        @JsonProperty("userId") String userId,       // Qui tape la commande ?
        @JsonProperty("containerId") String containerId,
        @JsonProperty("command") String command      // ex: "ls -la"
    ) {}

    // --- ENDPOINTS ---

    /**
     * WebSocket pour chat (inchangé)
     */
    @MessageMapping("/arena") 
    @SendTo("/topic/arena")  
    public String handleInput(String message) {
        return message;
    }

    /**
     * Démarrer le challenge
     */
    @PostMapping("/start/{challengeId}")
    public ResponseEntity<?> startArena(@PathVariable String challengeId) {
        try {
            String containerId = arenaService.startChallengeEnvironment(challengeId);
            return ResponseEntity.ok(Map.of("containerId", containerId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Arrêter le challenge
     */
    @PostMapping("/stop/{containerId}")
    public ResponseEntity<?> stopArena(@PathVariable String containerId) {
        arenaService.stopChallengeEnvironment(containerId);
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ TICKET W-02 : Exécuter une commande et notifier RabbitMQ
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeCommand(@RequestBody CommandRequest request) {
        try {
            // 1. SIGNALER L'ACTION (RabbitMQ) - Fire & Forget
            // L'IA écoutera ce message plus tard pour analyser le comportement
            rabbitMQProducer.sendGameEvent(request.userId(), request.command(), request.containerId());

            // 2. EXÉCUTER LA COMMANDE (Docker)
            String result = containerService.executeCommand(request.containerId(), request.command());

            return ResponseEntity.ok(Map.of("output", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur d'exécution: " + e.getMessage()));
        }
    }

    /**
     * Valider un flag
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateFlag(@RequestBody FlagRequest request) {
        boolean success = arenaService.validateFlag(request.userId(), request.challengeId(), request.flag());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Flag valide ! Points attribués.", "success", true));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Flag incorrect.", "success", false));
        }
    }
}