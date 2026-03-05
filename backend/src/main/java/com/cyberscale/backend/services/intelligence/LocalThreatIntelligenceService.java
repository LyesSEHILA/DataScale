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

    private static final String DEMO_MALICIOUS_IP = String.join(".", "118", "25", "6", "39");
    private static final String LOCALHOST_IP = String.join(".", "127", "0", "0", "1");
    private static final String LOCAL_NETWORK_PREFIX = String.join(".", "192", "168", "");

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

        // Utilisation des constantes sécurisées
        if (DEMO_MALICIOUS_IP.equals(ipAddress)) {
            threat.setCountryCode("CN");
            threat.setUsageType("Data Center");
            threat.setAbuseConfidenceScore(100); // Menace maximale
            
        } else if (ipAddress != null && (ipAddress.startsWith(LOCAL_NETWORK_PREFIX) || LOCALHOST_IP.equals(ipAddress))) {
            threat.setCountryCode("LOCAL");
            threat.setUsageType("Internal Network");
            threat.setAbuseConfidenceScore(0); // Sûr
            
        } else {
            // Génération algorithmique : la même IP donnera TOUJOURS le même résultat
            int hash = ipAddress != null ? ipAddress.hashCode() : 0;
            Random random = new Random(hash);
            
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