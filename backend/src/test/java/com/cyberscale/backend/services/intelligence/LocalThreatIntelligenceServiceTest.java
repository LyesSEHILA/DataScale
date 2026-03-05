package com.cyberscale.backend.services.intelligence;

import com.cyberscale.backend.models.DetectedThreat;
import com.cyberscale.backend.repositories.DetectedThreatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalThreatIntelligenceServiceTest {

    private static final String MALICIOUS_IP = "118.25.6.39";
    private static final String LOCAL_IP = "192.168.1.15";
    private static final String RANDOM_IP = "8.8.8.8";

    @Mock
    private DetectedThreatRepository repository;

    @InjectMocks
    private LocalThreatIntelligenceService service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.save(any(DetectedThreat.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnMaxThreat_ForMaliciousIp() {
        DetectedThreat threat = service.analyzeAndSaveIp(MALICIOUS_IP);

        assertNotNull(threat);
        assertEquals(MALICIOUS_IP, threat.getIpAddress());
        assertEquals("CN", threat.getCountryCode());
        assertEquals("Data Center", threat.getUsageType());
        assertEquals(100, threat.getAbuseConfidenceScore());
        
        verify(repository, times(1)).save(any(DetectedThreat.class));
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnZeroThreat_ForLocalIp() {
        DetectedThreat threat = service.analyzeAndSaveIp(LOCAL_IP);

        assertNotNull(threat);
        assertEquals(LOCAL_IP, threat.getIpAddress());
        assertEquals("LOCAL", threat.getCountryCode());
        assertEquals("Internal Network", threat.getUsageType());
        assertEquals(0, threat.getAbuseConfidenceScore());
        
        verify(repository, times(1)).save(any(DetectedThreat.class));
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnConsistentRandomThreat_ForOtherIp() {
        DetectedThreat threat1 = service.analyzeAndSaveIp(RANDOM_IP);
        DetectedThreat threat2 = service.analyzeAndSaveIp(RANDOM_IP);

        assertNotNull(threat1);
        assertEquals(RANDOM_IP, threat1.getIpAddress());
        
        assertEquals(threat1.getCountryCode(), threat2.getCountryCode());
        assertEquals(threat1.getUsageType(), threat2.getUsageType());
        assertEquals(threat1.getAbuseConfidenceScore(), threat2.getAbuseConfidenceScore());
        
        verify(repository, times(2)).save(any(DetectedThreat.class));
    }
}