package com.cyberscale.backend.services;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;


/**
 * Service gérant la logique des exercices de Phishing.
 * Il stocke les scénarios, les zones piégées et fournit le feedback pédagogique.
 */
@Service
public class PhishingService {

    private final Map<String, Map<String, String>> scenarioTraps = Map.of(
        "SCENARIO_1", Map.of(
            "sender-email", "L'adresse 'pypal-support.com' est fausse. Le vrai domaine est paypal.com.",
            "urgency-text", "L'urgence ('24h pour agir') est une technique classique pour provoquer la panique.",
            "generic-greeting", "Un service officiel utilise souvent votre nom, pas 'Cher Client'.",
            "fake-link-btn", "Ce bouton ne redirige pas vers PayPal. Survolez-le pour voir la vraie URL."
        ),
        "SCENARIO_2", Map.of(
            "sender-hr", "L'email RH vient de gmail.com, pas de l'entreprise.",
            "attachment-exe", "Un fichier .exe (exécutable) n'est jamais une facture. C'est un virus.",
            "typo-body", "Il y a des fautes d'orthographe flagrantes ('votre salair')."
        ),
        "SCENARIO_3", Map.of(
            "fake-ceo", "Le PDG ne vous demanderait jamais un virement urgent par email simple.",
            "iban-foreign", "L'IBAN est situé dans un pays étranger (LT = Lituanie) alors que le fournisseur est local."
        )
    );

    private final Map<String, String> scenarioLessons = Map.of(
        "SCENARIO_1", "Leçon : Le 'Typosquatting' consiste à utiliser des noms de domaine très proches des vrais (ex: pypal au lieu de paypal). Vérifiez toujours l'expéditeur.",
        "SCENARIO_2", "Leçon : Les pièces jointes sont le vecteur n°1 des Ransomwares. N'ouvrez jamais un .exe, .scr ou .js reçu par email.",
        "SCENARIO_3", "Leçon : C'est une 'Fraude au Président'. L'attaquant se fait passer pour un dirigeant pour contourner les procédures de sécurité."
    );

    /**
     * Analyse un clic utilisateur sur un élément d'un email de phishing.
     * @param scenarioId L'identifiant du scénario en cours (ex: SCENARIO_1).
     * @param elementId  L'ID HTML de l'élément cliqué par l'utilisateur.
     * @return Une Map contenant le résultat ("isTrap": boolean) et l'explication ("message").
     */
    public Map<String, Object> analyzeClick(String scenarioId, String elementId) {
        if (scenarioId == null || elementId == null) return Map.of("status", "error");

        Map<String, String> traps = scenarioTraps.get(scenarioId);
        
        if (traps != null && traps.containsKey(elementId)) {
            // C'est un piège !
            return Map.of(
                "isTrap", true,
                "message", "Bien vu ! " + traps.get(elementId)
            );
        } else {
            // Ce n'est pas un piège
            return Map.of(
                "isTrap", false,
                "message", "Cet élément semble légitime. Concentrez-vous sur les anomalies."
            );
        }
    }

    /**
     * Récupère les métadonnées d'un scénario (nombre de pièges, leçon finale).
     * @param scenarioId L'identifiant du scénario.
     * @return Une Map avec le total des pièges et la leçon, ou null si introuvable.
     */
    public Map<String, Object> getScenarioInfo(String scenarioId) {
        Map<String, String> traps = scenarioTraps.get(scenarioId);
        if (traps == null) return null;

        return Map.of(
            "totalTraps", traps.size(),
            "lesson", scenarioLessons.getOrDefault(scenarioId, "Restez vigilant.")
        );
    }
}