package com.cyberscale.backend.services.intelligence;

import com.cyberscale.backend.models.DetectedThreat;
import com.cyberscale.backend.repositories.DetectedThreatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocalThreatIntelligenceServiceTest {

    @Mock
    private DetectedThreatRepository repository;

    @InjectMocks
    private LocalThreatIntelligenceService service;

    @Test
    void analyzeAndSaveIp_ShouldReturnNull_WhenIpIsNull() {
        DetectedThreat result = service.analyzeAndSaveIp(null);

        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnNull_WhenIpIsEmpty() {
        DetectedThreat result = service.analyzeAndSaveIp("   ");

        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnMaliciousProfile_WhenSpecificIp() {
        when(repository.save(any(DetectedThreat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DetectedThreat result = service.analyzeAndSaveIp("118.25.6.39");

        assertNotNull(result);
        assertEquals("CN", result.getCountryCode());
        assertEquals("Data Center", result.getUsageType());
        assertEquals(100, result.getAbuseConfidenceScore());
        verify(repository, times(1)).save(any(DetectedThreat.class));
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnSafeProfile_WhenLocalIp192() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DetectedThreat result = service.analyzeAndSaveIp("192.168.1.50");

        assertNotNull(result);
        assertEquals("LOCAL", result.getCountryCode());
        assertEquals("Internal Network", result.getUsageType());
        assertEquals(0, result.getAbuseConfidenceScore());
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnSafeProfile_WhenLocalIp10() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DetectedThreat result = service.analyzeAndSaveIp("10.0.0.5");

        assertNotNull(result);
        assertEquals("LOCAL", result.getCountryCode());
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnSafeProfile_WhenLocalhost() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DetectedThreat result = service.analyzeAndSaveIp("127.0.0.1");

        assertNotNull(result);
        assertEquals("LOCAL", result.getCountryCode());
    }

    @Test
    void analyzeAndSaveIp_ShouldReturnGeneratedProfile_WhenOtherIp() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DetectedThreat result = service.analyzeAndSaveIp("8.8.8.8");

        assertNotNull(result);
        assertNotEquals("LOCAL", result.getCountryCode());
        assertNotNull(result.getCountryCode());
        assertNotNull(result.getUsageType());
        assertTrue(result.getAbuseConfidenceScore() >= 0 && result.getAbuseConfidenceScore() <= 100);
        assertNotNull(result.getDetectedAt());
    }
}