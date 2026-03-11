package com.cyberscale.backend.services.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cyberscale.backend.dto.GameEventDTO;
import com.cyberscale.backend.services.ai.HuggingFaceClient;

@Service
public class RabbitMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final HuggingFaceClient aiClient;
    private final RabbitMQProducer producer;
    private final SimpMessagingTemplate messagingTemplate;

    public RabbitMQConsumer(HuggingFaceClient aiClient, RabbitMQProducer producer, SimpMessagingTemplate messagingTemplate) {
        this.aiClient = aiClient;
        this.producer = producer;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveGameEvent(GameEventDTO event) {
        String rawMessage = event.getAction();
        
        String mode = "TUTORIAL";
        String command = rawMessage;
        boolean isHelpRequest = false;

        if (rawMessage.contains("|")) {
            String[] parts = rawMessage.split("\\|", 2);
            mode = parts[0];
            command = parts[1];
        }

        if (command.startsWith("HELP|")) {
            isHelpRequest = true;
            command = command.substring(5);
        }

        logger.info("🎮 Mode: {} | Help: {} | Cible: {}", mode, isHelpRequest, command);

        String prompt = "";

        // --- GÉNÉRATION DU PROMPT STRICT (PROMPT ENGINEERING) ---
        
        String frenchRule = "RÉPONDS OBLIGATOIREMENT ET STRICTEMENT EN FRANÇAIS. ";

        if (isHelpRequest) {
            if (command.equals("GENERAL_CONTEXT") || command.isEmpty()) {
                prompt = frenchRule + """
                    Tu es un mentor expert en Cybersécurité. L'utilisateur est en mode %s et demande de l'aide générale.
                    Réponds OBLIGATOIREMENT avec ce format exact :
                    OBJECTIF: <1 phrase EN FRANÇAIS donnant un but clair>
                    COMMANDE: <la commande suggérée>
                    Ne fais aucune phrase d'introduction ni de conclusion.
                    """.formatted(mode);
            } else {
                prompt = frenchRule + """
                    Tu es un mentor expert en Cybersécurité. L'utilisateur a tapé la commande : '%s'.
                    Réponds OBLIGATOIREMENT avec ce format exact :
                    EXPLICATION: <1 phrase très courte EN FRANÇAIS expliquant la commande>
                    ASTUCE: <1 conseil EN FRANÇAIS ou la prochaine commande logique>
                    Ne fais aucune phrase d'introduction ni de conclusion.
                    """.formatted(command);
            }
        } else {
            switch (mode) {
                case "TUTORIAL":
                    prompt = frenchRule + """
                        Tu es un Instructeur Linux. L'élève a tapé : '%s'.
                        Réponds OBLIGATOIREMENT avec ce format exact :
                        EXPLICATION: <1 phrase très courte et simple EN FRANÇAIS expliquant la commande>
                        Ne dis rien d'autre.
                        """.formatted(command);
                    break;
                case "RED_TEAM":
                    prompt = frenchRule + """
                        Tu es une IA Blue Team (SysAdmin). Un attaquant a tapé : '%s'.
                        Contre-attaque ou trace-le.
                        Réponds OBLIGATOIREMENT avec ce format exact :
                        COMMANDE: <1 seule commande Linux défensive>
                        EXPLICATION: <1 phrase très courte EN FRANÇAIS expliquant ta défense>
                        Ne donne aucune explication en anglais, ne dis rien d'autre.
                        """.formatted(command);
                    break;
                case "BLUE_TEAM":
                    prompt = frenchRule + """
                        Tu es une IA Red Team (Hacker). Le défenseur a tapé : '%s'.
                        Attaque-le.
                        Réponds OBLIGATOIREMENT avec ce format exact :
                        COMMANDE: <1 seule commande Linux offensive>
                        EXPLICATION: <1 phrase très courte EN FRANÇAIS expliquant ton attaque>
                        Ne donne aucune explication en anglais, ne dis rien d'autre.
                        """.formatted(command);
                    break;
                default:
                    prompt = "Réponds par une commande Linux courte. Explique-la en français.";
            }
        }

        try {
            // Appel IA
            String response = aiClient.generateResponse(prompt);
            String cleanResponse = response.replaceAll("```bash", "").replaceAll("```", "").trim();

            logger.info("🤖 [IA] Réponse brute : {}", cleanResponse);

            // --- PARSING DE LA RÉPONSE (Séparation Commande / Explication) ---
            String infraCommand = cleanResponse; // Par défaut
            String displayMessage = cleanResponse;

            // Si on est en mode combat (et non en mode aide), on sépare la commande de l'explication
            if (!isHelpRequest && (mode.equals("RED_TEAM") || mode.equals("BLUE_TEAM"))) {
                if (cleanResponse.contains("COMMANDE:") && cleanResponse.contains("EXPLICATION:")) {
                    try {
                        String[] parts = cleanResponse.split("EXPLICATION:");
                        infraCommand = parts[0].replace("COMMANDE:", "").trim();
                        String explanation = parts[1].trim();
                        
                        // Mise en forme propre pour l'interface web
                        displayMessage = "💻 " + infraCommand + "\n\n📖 " + explanation;
                    } catch (Exception e) {
                        logger.warn("⚠️ Impossible de parser la réponse de l'IA : {}", cleanResponse);
                    }
                }
            }

            // LOGIQUE D'ENVOI
            if (isHelpRequest || mode.equals("TUTORIAL")) {
                String icon = isHelpRequest ? "💡 CONSEIL :\n\n" : "ℹ️ INFO :\n\n";
                messagingTemplate.convertAndSend("/topic/arena/alerts", icon + displayMessage);
            } else {
                // On exécute UNIQUEMENT la commande bash dans Docker
                logger.info("💀 Envoi infra (Bash pur) : {}", infraCommand);
                producer.sendInfraCommand(infraCommand);
                
                // On affiche la commande ET l'explication au joueur
                messagingTemplate.convertAndSend("/topic/arena/alerts", "⚠️ ACTION ADVERSE :\n\n" + displayMessage);
            }

        } catch (Exception e) {
            logger.error("❌ Erreur IA : {}", e.getMessage());
        }
    }
}