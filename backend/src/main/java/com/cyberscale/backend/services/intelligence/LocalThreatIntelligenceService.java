package com.cyberscale.backend.services.intelligence;

import com.cyberscale.backend.models.DetectedThreat;
import com.cyberscale.backend.repositories.DetectedThreatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class LocalThreatIntelligenceService {

    private static final Logger logger = LoggerFactory.getLogger(LocalThreatIntelligenceService.class);
    private final DetectedThreatRepository repository;

    // Listes pour simuler des données réalistes
    private final List<String> fakeCountries = List.of("FR", "US", "CN", "RU", "BR", "DE", "IN");
    private final List<String> fakeUsageTypes = List.of("Data Center", "Residential", "Commercial", "VPN", "TOR Node");

    public LocalThreatIntelligenceService(DetectedThreatRepository repository) {
        this.repository = repository;
    }

    /**
     * Analyse l'IP localement et génère un profil de menace sans appel externe.
     */
    public DetectedThreat analyzeAndSaveIp(String ipAddress) {
        logger.info("🔒 [MODE PRIVÉ] Analyse locale de l'IP: {}", ipAddress);

        DetectedThreat threat = new DetectedThreat();
        threat.setIpAddress(ipAddress);
        threat.setDetectedAt(LocalDateTime.now());

        // Cas spécifiques pour votre démonstration (IPs "scénarisées")
        if (ipAddress.equals("118.25.6.39")) {
            threat.setCountryCode("CN");
            threat.setUsageType("Data Center");
            threat.setAbuseConfidenceScore(100); // Menace maximale
        } else if (ipAddress.startsWith("192.168.") || ipAddress.equals("127.0.0.1")) {
            threat.setCountryCode("LOCAL");
            threat.setUsageType("Internal Network");
            threat.setAbuseConfidenceScore(0); // Sûr
        } else {
            // Génération algorithmique : la même IP donnera TOUJOURS le même résultat
            // On utilise le hash de l'IP comme "graine" (seed) pour le générateur aléatoire
            Random random = new Random(ipAddress.hashCode());
            
            threat.setCountryCode(fakeCountries.get(random.nextInt(fakeCountries.size())));
            threat.setUsageType(fakeUsageTypes.get(random.nextInt(fakeUsageTypes.size())));
            threat.setAbuseConfidenceScore(random.nextInt(101)); // Score entre 0 et 100
        }

        logger.info("Résultat local pour {} -> Score: {}%, Pays: {}, Type: {}", 
                ipAddress, threat.getAbuseConfidenceScore(), threat.getCountryCode(), threat.getUsageType());

        // Sauvegarde en base de données
        return repository.save(threat);
    }
}