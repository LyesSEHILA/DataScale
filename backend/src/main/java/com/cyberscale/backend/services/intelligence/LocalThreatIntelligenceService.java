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

    private static final String DEMO_MALICIOUS_IP = String.join(".", "118", "25", "6", "39");
    private static final String LOCALHOST_IP = String.join(".", "127", "0", "0", "1");
    private static final String LOCAL_NETWORK_PREFIX = String.join(".", "192", "168", "");

    private final List<String> fakeCountries = List.of("FR", "US", "CN", "RU", "BR", "DE", "IN");
    private final List<String> fakeUsageTypes = List.of("Data Center", "Residential", "Commercial", "VPN", "TOR Node");

    public LocalThreatIntelligenceService(DetectedThreatRepository repository) {
        this.repository = repository;
    }


    public DetectedThreat analyzeAndSaveIp(String ipAddress) {
        logger.info("🔒 [MODE PRIVÉ] Analyse locale de l'IP: {}", ipAddress);

        DetectedThreat threat = new DetectedThreat();
        threat.setIpAddress(ipAddress);
        threat.setDetectedAt(LocalDateTime.now());

        if (DEMO_MALICIOUS_IP.equals(ipAddress)) {
            threat.setCountryCode("CN");
            threat.setUsageType("Data Center");
            threat.setAbuseConfidenceScore(100); 
            
        } else if (ipAddress != null && (ipAddress.startsWith(LOCAL_NETWORK_PREFIX) || LOCALHOST_IP.equals(ipAddress))) {
            threat.setCountryCode("LOCAL");
            threat.setUsageType("Internal Network");
            threat.setAbuseConfidenceScore(0); 
            
        } else {

            int hash = ipAddress != null ? (ipAddress.hashCode() & 0x7fffffff) : 0;
            
            threat.setCountryCode(fakeCountries.get(hash % fakeCountries.size()));
            threat.setUsageType(fakeUsageTypes.get((hash / 2) % fakeUsageTypes.size()));
            threat.setAbuseConfidenceScore((hash / 3) % 101); // Score de 0 à 100
        }

        logger.info("Résultat local pour {} -> Score: {}%, Pays: {}, Type: {}", 
                ipAddress, threat.getAbuseConfidenceScore(), threat.getCountryCode(), threat.getUsageType());

        return repository.save(threat);
    }
}