package com.cyberscale.backend.controllers;

import com.cyberscale.backend.models.EmailScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cyberscale.backend.services.PhishingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phishing")
@CrossOrigin(origins = "*")
public class PhishingController {

    private static final Logger logger = LoggerFactory.getLogger(PhishingController.class);

    @Autowired
    private PhishingService phishingService;

    @GetMapping("/inbox")
    public ResponseEntity<List<EmailScenario>> getInbox() {
        return ResponseEntity.ok(phishingService.getAllEmails());
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeClick(@RequestBody Map<String, Object> request) {
        Long scenarioId = Long.valueOf(request.get("scenarioId").toString());
        String elementId = (String) request.get("elementId");
        return ResponseEntity.ok(phishingService.analyzeClick(scenarioId, elementId));
    }

    @GetMapping("/info/{scenarioId}")
    public ResponseEntity<?> getScenarioInfo(@PathVariable Long scenarioId) {
        Map<String, Object> info = phishingService.getScenarioInfo(scenarioId);
        if (info == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(info);
    }
}