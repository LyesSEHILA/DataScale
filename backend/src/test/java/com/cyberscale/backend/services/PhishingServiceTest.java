package com.cyberscale.backend.services;

import com.cyberscale.backend.models.EmailScenario;
import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.repositories.EmailScenarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhishingServiceTest {

    @Mock
    private EmailScenarioRepository repository;

    @InjectMocks
    private PhishingService phishingService;

    private EmailScenario mockScenario;

    @BeforeEach
    void setUp() {
        mockScenario = new EmailScenario();
        mockScenario.setId(1L);
        mockScenario.setSender("Support IT <security@fake.com>");
        mockScenario.setSubject("Alerte Sécurité");
        mockScenario.setContentHtml("<p>Cliquez ici</p>");
        mockScenario.setTraps(List.of(
            Map.of("id", "btn-verify", "message", "Lien frauduleux")
        ));
    }

    @Test
    void generateRandomPhishingEmail_ShouldSaveAndReturnScenario() {
        when(repository.save(any(EmailScenario.class))).thenReturn(mockScenario);

        EmailScenario result = phishingService.generateRandomPhishingEmail();

        assertNotNull(result);
        assertEquals("Alerte Sécurité", result.getSubject());
        verify(repository, times(1)).save(any(EmailScenario.class));
    }

    @Test
    void getAllEmails_ShouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(mockScenario));

        List<EmailScenario> result = phishingService.getAllEmails();

        assertEquals(1, result.size());
        assertEquals(mockScenario.getId(), result.get(0).getId());
    }

    @Test
    void analyzeClick_ShouldReturnTrapFeedback_WhenIdMatches() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockScenario));

        Map<String, Object> result = phishingService.analyzeClick(1L, "btn-verify");

        assertTrue((Boolean) result.get("isTrap"));
        assertTrue(result.get("message").toString().contains("Lien frauduleux"));
    }

    @Test
    void analyzeClick_ShouldReturnSafeFeedback_WhenIdDoesNotMatch() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockScenario));

        Map<String, Object> result = phishingService.analyzeClick(1L, "safe-element");

        assertFalse((Boolean) result.get("isTrap"));
        assertTrue(result.get("message").toString().contains("correct"));
    }

    @Test
    void analyzeClick_ShouldReturnError_WhenInputsAreNull() {
        Map<String, Object> result = phishingService.analyzeClick(null, null);
        assertEquals("error", result.get("status"));
    }

    @Test
    void getScenarioInfo_ShouldReturnMap_WhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockScenario));

        Map<String, Object> result = phishingService.getScenarioInfo(1L);

        assertNotNull(result);
        assertEquals(1, result.get("totalTraps"));
        assertEquals("Alerte Sécurité", result.get("subject"));
    }

    @Test
    void getScenarioInfo_ShouldReturnNull_WhenNotExists() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Map<String, Object> result = phishingService.getScenarioInfo(99L);

        assertNull(result);
    }
}