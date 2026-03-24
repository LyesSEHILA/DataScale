package com.cyberscale.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Random;

/**
 * Service émettant des notifications de phishing en temps réel via WebSockets.
 */
@Service
public class PhishingNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PhishingNotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final PhishingService phishingService;
    private final Random random = new Random();

    @Autowired
    public PhishingNotificationService(SimpMessagingTemplate messagingTemplate, PhishingService phishingService) {
        this.messagingTemplate = messagingTemplate;
        this.phishingService = phishingService;
    }

    /**
     * Génère un premier mail dès le démarrage pour éviter une boîte vide.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info(" Initialisation du module Phishing...");
        phishingService.generateRandomPhishingEmail();
    }

    /**
     * Envoie une alerte de phishing toutes les 2 heures (7200000ms).
     */
    @Scheduled(fixedRate = 7200000) 
    public void sendPhishingAlert() {
        // GÉNÉRATION D'UN NOUVEAU MAIL ALÉATOIRE
        com.cyberscale.backend.models.EmailScenario scenario = phishingService.generateRandomPhishingEmail();
        
        logger.info(" Nouvelle génération Phishing : ID {}", scenario.getId());

        messagingTemplate.convertAndSend("/topic/phishing-alerts", Map.of(
            "scenarioId", scenario.getId(),
            "title", "Nouvel email suspect reçu",
            "message", "De : " + scenario.getSender(),
            "icon", "fas fa-envelope-open-text"
        ));
    }

    /**
     * Méthode manuelle pour tester la notification (optionnel).
     */
    public void triggerManualAlert(String scenarioId) {
        messagingTemplate.convertAndSend("/topic/phishing-alerts", Map.of(
            "scenarioId", scenarioId,
            "title", "Alerte de sécurité critique",
            "message", "Une tentative d'hameçonnage a été détectée sur votre compte.",
            "icon", "fas fa-shield-virus"
        ));
    }
}