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

/**
 * Controleur REST gérant le module de sensibilisation au Phishing.
 * Il expose les endpoints pour :
 * - Analyser les cliques de l'utilisateur sur les emails simules.
 * - Récupérer les objectifs pédagogiques d'un scénario.
 */
@RestController
@RequestMapping("/api/phishing")
@CrossOrigin(origins = "*")
public class PhishingController {

    @Autowired
    private PhishingService phishingService;

    /**
     * Analyse un clic sur un élément d'email.
     * Détermine si l'élément était un piège et renvoie le feedback éducatif.
     * @param request DTO contenant l'ID du scénario et l'ID de l'element cliqué.
     * @return Une Map contenant "isTrap" et "message".
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeClick(@RequestBody PhishingRequest request) {
        return ResponseEntity.ok(phishingService.analyzeClick(request.scenarioId(), request.elementId()));
    }

    /**
     * Récupère les informations globales d'un scénario.
     * @param scenarioId L'identifiant du scénario.
     * @return Une Map contenant les métadonnées du scénario.
     */
    @GetMapping("/info/{scenarioId}")
    public ResponseEntity<?> getScenarioInfo(@PathVariable String scenarioId) {
        return ResponseEntity.ok(phishingService.getScenarioInfo(scenarioId));
    }
}