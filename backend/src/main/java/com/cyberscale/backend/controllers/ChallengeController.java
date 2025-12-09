package com.cyberscale.backend.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.services.ArenaService;

@RestController
@RequestMapping("/api/challenges")
@CrossOrigin(origins = "*")
public class ChallengeController {

    @Autowired private ArenaService arenaService;

    // Liste pour le Dashboard
    @GetMapping
    public ResponseEntity<List<ChallengeDTO>> getAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(arenaService.getAllChallenges(userId));
    }

    // Détail pour l'Arena (Explication)
    @GetMapping("/{id}")
    public ResponseEntity<ChallengeDTO> getOne(@PathVariable String id) {
        return ResponseEntity.ok(arenaService.getChallengeById(id));
    }
}