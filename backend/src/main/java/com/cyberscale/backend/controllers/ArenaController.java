package com.cyberscale.backend.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cyberscale.backend.services.ArenaService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * Controleur REST et WebSocket gérant l'arene de jeu.
 * Responsabilités :
 * - REST : Démarrer/Arrêter les conteneurs Docker des challenges.
 * - REST : Valider les Flags soumis par les joueurs.
 * - WS : Gérer les communications temps réel via STOMP.
 */
@RestController
@RequestMapping("/api/arena")
@CrossOrigin(origins = "*") 
public class ArenaController {

    @Autowired private ArenaService arenaService;

    /**
     * Endpoint WebSocket pour l'echo ou le chat de l'arène.
     * Les messages envoyés à "/app/arena" sont redistribues sur "/topic/arena".
     * @param message Le message entrant.
     * @return Le message sortant.
     */
    @MessageMapping("/arena") 
    @SendTo("/topic/arena")  
    public String handleInput(String message) {
        return message;
    }
    
    public record FlagRequest(
        @JsonProperty("userId") Long userId,
        @JsonProperty("challengeId") String challengeId,
        @JsonProperty("flag") String flag
    ) {}

    /**
     * Démarre l'environnement pour un challenge spécifique.
     * @param challengeId L'ID du challenge à lancer.
     * @return L'ID du conteneur crée pour permettre au front de s'y connecter.
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
     * Arrête et supprime un conteneur actif.
     * Appele lorsque l'utilisateur quitte la page du challenge.
     * @param containerId L'ID du conteneur a detruire.
     * @return code HTTP 200.
     */
    @PostMapping("/stop/{containerId}")
    public ResponseEntity<?> stopArena(@PathVariable String containerId) {
        arenaService.stopChallengeEnvironment(containerId);
        return ResponseEntity.ok().build();
    }

    /**
     * Valide un flag soumis par l'utilisateur.
     * @param request DTO contenant user, challenge et flag.
     * @return code HTTP 200 ou 400 .
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