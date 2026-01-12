package com.cyberscale.backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.services.LogGenerator;

@RestController
@RequestMapping("/api/challenges")
@CrossOrigin(origins = "*")
public class ChallengeController {

    @Autowired private ArenaService arenaService;
    @Autowired private LogGenerator logGenerator;

    @GetMapping
    public ResponseEntity<List<ChallengeDTO>> getAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(arenaService.getAllChallenges(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeDTO> getOne(@PathVariable String id) {
        return ResponseEntity.ok(arenaService.getChallengeById(id));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<String>> getChallengeLogs() {
        return ResponseEntity.ok(logGenerator.generateLogs());
    }

    @PostMapping("/logs/validate")
    public ResponseEntity<?> validateAttackerIp(@RequestBody Map<String, String> payload) {
        String submittedIp = payload.get("ip");
        String actualAttackerIp = logGenerator.getAttackerIp(); 

        if (submittedIp != null && submittedIp.equals(actualAttackerIp)) {
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Bravo ! IP de l'attaquant identifiée."
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Ce n'est pas l'IP suspecte. Cherchez les erreurs 404 répétées."
            ));
        }
    }
}