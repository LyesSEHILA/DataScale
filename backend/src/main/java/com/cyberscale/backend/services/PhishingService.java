package com.cyberscale.backend.services;

import com.cyberscale.backend.models.EmailScenario;
import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.repositories.EmailScenarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service gérant la logique des exercices de Phishing.
 */
@Service
public class PhishingService {

    private static final Logger logger = LoggerFactory.getLogger(PhishingService.class);

    @Autowired
    private EmailScenarioRepository repository;

    private final Random random = new Random();

    private final String[] SENDERS = {"Support Microsoft", "Banque Populaire", "Service RH", "PayPal Support", "IT Department", "Amazon Prime", "Netflix France"};
    private final String[] SUBJECTS = {"Action requise : Sécurité compte", "Facture impayée", "Virement en attente de validation", "Votre abonnement expire", "Tentative de connexion bloquée"};
    private final String[] MALICIOUS_DOMAINS = {"security-check.net", "update-account.com", "billing-support.org", "verify-identity.io"};

    public EmailScenario generateRandomPhishingEmail() {
        String senderName = SENDERS[random.nextInt(SENDERS.length)];
        String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
        String domain = MALICIOUS_DOMAINS[random.nextInt(MALICIOUS_DOMAINS.length)];
        String fakeEmail = senderName.toLowerCase().replace(" ", ".") + "@" + domain;

        EmailScenario scenario = new EmailScenario();
        scenario.setSender(senderName + " <" + fakeEmail + ">");
        scenario.setSubject(subject);
        scenario.setDifficulty(IQuestion.DifficultyQuestion.values()[random.nextInt(3)]);

        List<Map<String, Object>> traps = new ArrayList<>();
        StringBuilder html = new StringBuilder();

        int templateType = random.nextInt(3);
        logger.info("Génération Phishing : Template {}", templateType);

        if (templateType == 0) {
            html.append("<p>Bonjour, nous avons détecté une activité suspecte sur votre compte.</p>");
            html.append("<p>Veuillez cliquer sur le bouton ci-dessous pour sécuriser vos données :</p>");
            html.append("<a id='btn-verify' class='interactive' href='#'>Vérifier mon compte</a>");
            
            traps.add(Map.of("id", "btn-verify", "message", "L'URL de redirection est frauduleuse. Elle ne mène pas vers le site officiel."));
            traps.add(Map.of("id", "sender-email", "message", "L'adresse email '" + fakeEmail + "' utilise un nom de domaine suspect."));
        } else if (templateType == 1) {
            html.append("<p>Votre facture du mois est disponible.</p>");
            html.append("<p>Merci de consulter la pièce jointe pour éviter toute coupure de service.</p>");
            html.append("<div id='attach-1' class='interactive' style='border:1px solid #ccc; padding:10px; width:150px;'>Facture_992.exe</div>");
            
            traps.add(Map.of("id", "attach-1", "message", "Un fichier .exe n'est jamais une facture. C'est un virus exécutable."));
        } else {
            html.append("<p>URGENT : Un virement de 1450,00€ a été initié.</p>");
            html.append("<p>Si vous n'êtes pas à l'origine de cette demande, <span id='link-cancel' class='interactive'>annulez l'opération ici</span>.</p>");
            
            traps.add(Map.of("id", "link-cancel", "message", "L'urgence est utilisée ici pour vous faire paniquer et cliquer sans réfléchir."));
        }

        scenario.setContentHtml(html.toString());
        scenario.setTraps(traps);

        return repository.save(scenario);
    }

    public List<EmailScenario> getAllEmails() {
        return repository.findAll();
    }

    public Map<String, Object> analyzeClick(Long scenarioId, String elementId) {
        if (scenarioId == null || elementId == null) return Map.of("status", "error");
        
        Optional<EmailScenario> scenarioOpt = repository.findById(scenarioId);
        if (scenarioOpt.isEmpty()) return Map.of("isTrap", false, "message", "Scénario introuvable.");

        EmailScenario scenario = scenarioOpt.get();
        for (Map<String, Object> trap : scenario.getTraps()) {
            if (elementId.equals(trap.get("id")) || elementId.contains("sender")) {
                return Map.of("isTrap", true, "message", "Bien vu ! " + trap.get("message"));
            }
        }

        return Map.of("isTrap", false, "message", "Cet élément semble correct.");
    }

    public Map<String, Object> getScenarioInfo(Long scenarioId) {
        Optional<EmailScenario> scenarioOpt = repository.findById(scenarioId);
        if (scenarioOpt.isEmpty()) return null;

        EmailScenario scenario = scenarioOpt.get();
        Map<String, Object> info = new HashMap<>();
        info.put("totalTraps", scenario.getTraps() != null ? scenario.getTraps().size() : 0);
        info.put("lesson", "Vérifiez toujours l'expéditeur et ne cliquez pas sous la pression.");
        info.put("contentHtml", scenario.getContentHtml());
        info.put("subject", scenario.getSubject());
        info.put("sender", scenario.getSender());
            
        return info;
    }
}