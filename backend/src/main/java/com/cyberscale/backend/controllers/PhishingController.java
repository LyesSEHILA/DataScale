package com.cyberscale.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.dto.PhishingRequest;
import com.cyberscale.backend.services.PhishingService;

@RestController
@RequestMapping("/api/phishing")
@CrossOrigin(origins = "*")
public class PhishingController {

    @Autowired
    private PhishingService phishingService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeClick(@RequestBody PhishingRequest request) {
        return ResponseEntity.ok(phishingService.analyzeClick(request.scenarioId(), request.elementId()));
    }

    // Nouvel endpoint pour savoir combien de pièges il y a au total
    @GetMapping("/info/{scenarioId}")
    public ResponseEntity<?> getScenarioInfo(@PathVariable String scenarioId) {
        return ResponseEntity.ok(phishingService.getScenarioInfo(scenarioId));
    }
}