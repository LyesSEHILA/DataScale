package com.cyberscale.backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.services.ArenaService;
import com.cyberscale.backend.services.LogGenerator;

/**
 * Controleur REST gérant l'accès aux challenges.
 * Il expose les endpoints pour :
 * - Lister les challenges disponibles.
 * - Récupérer le détail d'un challenge.
 * - Gérer spécifiquement le jeu "Analyse de Logs".
 */
@RestController
@RequestMapping("/api/challenges")
@CrossOrigin(origins = "*")
public class ChallengeController {

    @Autowired private ArenaService arenaService;
    @Autowired private LogGenerator logGenerator;

    /**
     * Récupère la liste de tous les challenges disponibles.
     * Si un userId est fourni, indique quels challenges sont déjà validés.
     * @param userId ID de l'utilisateur pour contextualiser l'affichage.
     * @return Liste de DTOs des challenges.
     */
    @GetMapping
    public ResponseEntity<List<ChallengeDTO>> getAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(arenaService.getAllChallenges(userId));
    }

    /**
     * Récupère les détails d'un challenge spécifique.
     * @param id L'identifiant du challenge.
     * @return Le DTO du challenge.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChallengeDTO> getOne(@PathVariable String id) {
        return ResponseEntity.ok(arenaService.getChallengeById(id));
    }

    /**
     * Génère une séquence de logs simulés pour le challenge "Analyse de Logs".
     * @return Une liste de chaînes de caractères représentant des logs serveur.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<String>> getChallengeLogs() {
        return ResponseEntity.ok(logGenerator.generateLogs());
    }

    /**
     * Valide la réponse pour le challenge "Analyse de Logs".
     * L'utilisateur doit soumettre l'IP de l'attaquant qu'il a trouvée dans les logs.
     * @param payload Map contenant la clé "ip".
     * @return Un message de succès ou d'erreur avec un indice.
     */
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