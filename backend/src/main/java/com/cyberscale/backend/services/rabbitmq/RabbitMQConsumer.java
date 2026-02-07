package com.cyberscale.backend.services.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cyberscale.backend.dto.GameEventDTO; // 👈 IMPORTANT
import com.cyberscale.backend.services.ai.HuggingFaceClient;

@Service
public class RabbitMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final HuggingFaceClient aiClient;
    private final RabbitMQProducer producer;
    private final SimpMessagingTemplate messagingTemplate; // 👈 Pour le WebSocket

    // On ajoute messagingTemplate dans le constructeur
    public RabbitMQConsumer(HuggingFaceClient aiClient, RabbitMQProducer producer, SimpMessagingTemplate messagingTemplate) {
        this.aiClient = aiClient;
        this.producer = producer;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveGameEvent(GameEventDTO event) {
        String rawMessage = event.getAction();
        
        // Valeurs par défaut
        String mode = "TUTORIAL";
        String command = rawMessage;
        boolean isHelpRequest = false;

        // 1. Décodage du message (Format: "MODE|COMMANDE")
        if (rawMessage.contains("|")) {
            String[] parts = rawMessage.split("\\|", 2);
            mode = parts[0];
            command = parts[1];
        }

        // 2. Détection de la demande d'aide (Format spécial envoyé par askForHelp: "HELP|commande")
        // Attention : Si le frontend envoie "MODE|HELP|commande", il faut adapter le split.
        // Simplifions : Le frontend envoie dans le champ 'command' la valeur "HELP|ls"
        // Donc rawMessage reçu ici sera : "TUTORIAL|HELP|ls"
        
        if (command.startsWith("HELP|")) {
            isHelpRequest = true;
            command = command.substring(5); // On enlève "HELP|" pour garder juste "ls"
        }

        logger.info("🎮 Mode: {} | Help: {} | Cible: {}", mode, isHelpRequest, command);

        String prompt = "";

        // --- GÉNÉRATION DU PROMPT ---
        
        if (isHelpRequest) {
            // === MODE COACH / AIDE ===
            prompt = "Tu es un mentor expert en Cybersécurité. L'utilisateur est en mode " + mode + ". ";
            
            if (command.equals("GENERAL_CONTEXT") || command.isEmpty()) {
                prompt += "Il demande de l'aide générale. Donne-lui un objectif clair lié à son mode de jeu (Tutoriel, Red Team ou Blue Team) et suggère une commande à taper.";
            } else {
                prompt += "Il vient de taper la commande : '" + command + "' et demande de l'aide dessus. " +
                          "Explique brièvement à quoi elle sert, si elle était pertinente dans ce contexte, et donne une astuce ou une commande suivante logique.";
            }
            prompt += " Réponds en français, sois concis (max 2 phrases) et encourageant.";

        } else {
            // === MODE JEU / ACTION (Code précédent) ===
            switch (mode) {
                case "TUTORIAL":
                    prompt = "Tu es un Instructeur Linux. L'élève a tapé : '" + command + "'. " +
                             "Explique en 1 phrase simple le but de cette commande.";
                    break;
                case "RED_TEAM":
                    prompt = "Tu es une IA Blue Team (SysAdmin). Un attaquant a fait : '" + command + "'. " +
                             "Bloque-le ou trace-le. Réponds UNIQUEMENT par une commande Linux défensive.";
                    break;
                case "BLUE_TEAM":
                    prompt = "Tu es une IA Red Team (Hacker). Le défenseur a fait : '" + command + "'. " +
                             "Attaque-le. Réponds UNIQUEMENT par une commande Linux offensive.";
                    break;
                default:
                    prompt = "Réponds par une commande Linux.";
            }
        }

        try {
            // Appel IA
            String response = aiClient.generateResponse(prompt);
            
            // Nettoyage
            String cleanResponse = response.replaceAll("```bash", "").replaceAll("```", "").trim();

            logger.info("🤖 [IA] Réponse : {}", cleanResponse);

            // LOGIQUE D'ENVOI
            if (isHelpRequest || mode.equals("TUTORIAL")) {
                String icon = isHelpRequest ? "💡 " : "ℹ️ ";
                messagingTemplate.convertAndSend("/topic/arena/alerts", icon + cleanResponse);
            } else {
                // Si c'est du combat (Red/Blue Team), on exécute ET on affiche
                producer.sendInfraCommand(cleanResponse);
                messagingTemplate.convertAndSend("/topic/arena/alerts", "⚠️ " + cleanResponse);
            }

        } catch (Exception e) {
            logger.error("❌ Erreur IA : {}", e.getMessage());
        }
    }
}