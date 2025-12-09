package com.cyberscale.backend.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.services.ArenaService;

@RestController
@RequestMapping("/api/arena")
@CrossOrigin(origins = "*")
public class ArenaController {

    @Autowired private ArenaService arenaService;

    // DTO interne pour la requête
    public record FlagRequest(Long userId, String challengeId, String flag) {}

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