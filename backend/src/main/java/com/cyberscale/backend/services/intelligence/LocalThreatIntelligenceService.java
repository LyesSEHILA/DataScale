package com.cyberscale.backend.services.intelligence;

import com.cyberscale.backend.models.DetectedThreat;
import com.cyberscale.backend.repositories.DetectedThreatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        // Sécurité anti-crash si l'IP est nulle ou vide
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            logger.warn("Tentative d'analyse avec une IP nulle ou vide.");
            return null;
        }

        logger.info("🔒 [MODE PRIVÉ] Analyse locale de l'IP: {}", ipAddress);

        DetectedThreat threat = new DetectedThreat();
        threat.setIpAddress(ipAddress);
        threat.setDetectedAt(LocalDateTime.now());

        if ("118.25.6.39".equals(ipAddress)) {
            threat.setCountryCode("CN");
            threat.setUsageType("Data Center");
            threat.setAbuseConfidenceScore(100); 
        } else if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || "127.0.0.1".equals(ipAddress)) {
            threat.setCountryCode("LOCAL");
            threat.setUsageType("Internal Network");
            threat.setAbuseConfidenceScore(0); 
        } else {
            
            int hash = Math.abs(ipAddress.hashCode());
            
            threat.setCountryCode(fakeCountries.get(hash % fakeCountries.size()));
            threat.setUsageType(fakeUsageTypes.get((hash / 10) % fakeUsageTypes.size()));
            threat.setAbuseConfidenceScore(hash % 101); // Score entre 0 et 100
        }

        logger.info("Résultat local pour {} -> Score: {}%, Pays: {}, Type: {}", 
                ipAddress, threat.getAbuseConfidenceScore(), threat.getCountryCode(), threat.getUsageType());

        // Sauvegarde en base de données
        return repository.save(threat);
    }
}