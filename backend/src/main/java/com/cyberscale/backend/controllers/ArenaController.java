package com.cyberscale.backend.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Import global pour simplifier
import com.cyberscale.backend.services.ArenaService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

@RestController
@RequestMapping("/api/arena")
@CrossOrigin(origins = "*") // Autorise le frontend
public class ArenaController {

    @Autowired private ArenaService arenaService;

    @MessageMapping("/arena") // Écoute les messages envoyés sur "/app/arena"
    @SendTo("/topic/arena")   // Renvoie la réponse sur "/topic/arena"
    public String handleInput(String message) {
        // Pour l'instant, on fait un simple écho : le serveur renvoie ce qu'il reçoit.
        return message;
    }
    
    public record FlagRequest(
        @JsonProperty("userId") Long userId,
        @JsonProperty("challengeId") String challengeId,
        @JsonProperty("flag") String flag
    ) {}

    // --- NOUVEL ENDPOINT : Démarrer l'environnement ---
    @PostMapping("/start/{challengeId}")
    public ResponseEntity<?> startArena(@PathVariable String challengeId) {
        try {
            // Appel au service qui lance le Docker et retourne l'ID
            String containerId = arenaService.startChallengeEnvironment(challengeId);
            
            // On renvoie l'ID au frontend pour qu'il puisse se connecter au WebSocket
            return ResponseEntity.ok(Map.of("containerId", containerId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // --- NOUVEL ENDPOINT : Arrêter l'environnement (Optionnel mais propre) ---
    @PostMapping("/stop/{containerId}")
    public ResponseEntity<?> stopArena(@PathVariable String containerId) {
        arenaService.stopChallengeEnvironment(containerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateFlag(@RequestBody FlagRequest request) {
        // ... (votre code existant inchangé) ...
        boolean success = arenaService.validateFlag(request.userId(), request.challengeId(), request.flag());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Flag valide ! Points attribués.", "success", true));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Flag incorrect.", "success", false));
        }
    }

}