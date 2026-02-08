package com.cyberscale.backend.services;

import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.models.UserChallenge;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArenaServiceTest {

    @Mock private ContainerService containerService;
    @Mock private UserRepository userRepository;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserChallengeRepository userChallengeRepository;

    @InjectMocks private ArenaService arenaService;

    @Test
    void getAllChallengesShouldMapDifficultiesAndStatusCorrectly() {
        // GIVEN
        Challenge c1 = new Challenge("1", "Linux", "Desc", "F", 10);  // Facile
        Challenge c2 = new Challenge("2", "Web", "Desc", "F", 60);    // Moyen
        Challenge c3 = new Challenge("3", "Net", "Desc", "F", 150);   // Hardcore

        // ✅ CORRECTION 1 : Utilisation du constructeur (pas de setChallenge)
        UserChallenge uc = new UserChallenge(new User(), c1);

        when(challengeRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        when(userChallengeRepository.findByUserId(1L)).thenReturn(List.of(uc));

        // WHEN
        List<ChallengeDTO> result = arenaService.getAllChallenges(1L);

        // THEN
        assertEquals(3, result.size());
        assertEquals("FACILE", result.get(0).difficulty());
        assertEquals("MOYEN", result.get(1).difficulty());
        assertEquals("HARDCORE", result.get(2).difficulty());
        
        // ✅ CORRECTION 2 : isValidated() au lieu de isDone()
        assertTrue(result.get(0).isValidated());
        assertFalse(result.get(1).isValidated());
    }

    @Test
    void getAllChallengesWithNullUserShouldReturnEmptyStatus() {
        Challenge c1 = new Challenge("1", "Linux", "Desc", "F", 10);
        when(challengeRepository.findAll()).thenReturn(List.of(c1));

        List<ChallengeDTO> result = arenaService.getAllChallenges(null);

        // ✅ CORRECTION 3 : isValidated() au lieu de isDone()
        assertFalse(result.get(0).isValidated());
        verify(userChallengeRepository, never()).findByUserId(any());
    }

    @Test
    void getChallengeByIdSuccess() {
        Challenge c = new Challenge("1", "T", "D", "F", 10);
        when(challengeRepository.findById("1")).thenReturn(Optional.of(c));

        ChallengeDTO dto = arenaService.getChallengeById("1");
        assertNotNull(dto);
        assertEquals("1", dto.id());
    }

    @Test
    void getChallengeByIdNotFound() {
        when(challengeRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.getChallengeById("unknown"));
    }

    @Test
    void startChallengeEnvironmentSuccess() {
        Challenge c = new Challenge("c1", "T", "D", "F", 10);
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(c));
        when(containerService.createChallengeContainer(eq("c1"), anyString())).thenReturn("container-123");

        String id = arenaService.startChallengeEnvironment(1L, "c1");
        assertEquals("container-123", id);
    }

    @Test
    void startChallengeEnvironmentNotFound() {
        when(challengeRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.startChallengeEnvironment(1L, "unknown"));
    }

    @Test
    void stopChallengeEnvironmentSuccess() {
        arenaService.stopChallengeEnvironment("c1");
        verify(containerService).stopAndRemoveContainer("c1");
    }

    @Test
    void validateFlagDynamicSuccess() {
        // On simule le cycle complet : start -> validate
        Long userId = 1L;
        String challengeId = "c1";
        Challenge c = new Challenge(challengeId, "T", "D", "STATIC", 10);
        User u = new User(); u.setId(userId);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(c));
        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        when(containerService.createChallengeContainer(eq(challengeId), anyString())).thenReturn("cid");
        
        // 1. Start pour générer le flag dynamique
        arenaService.startChallengeEnvironment(userId, challengeId);
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(containerService).createChallengeContainer(any(), captor.capture());
        String generatedFlag = captor.getValue();

        // 2. Validate
        boolean res = arenaService.validateFlag(userId, challengeId, generatedFlag);
        assertTrue(res);
        verify(userRepository).save(u);
    }

    @Test
    void validateFlagStaticFallback() {
        Long userId = 1L;
        String challengeId = "c1";
        Challenge c = new Challenge(challengeId, "T", "D", "STATIC_FLAG", 10);
        User u = new User(); u.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(c));
        // Pas de flag dynamique en mémoire

        boolean res = arenaService.validateFlag(userId, challengeId, "STATIC_FLAG");
        assertTrue(res);
    }

    @Test
    void validateFlagUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.validateFlag(99L, "c1", "flag"));
    }

    @Test
    void validateFlagChallengeNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(challengeRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.validateFlag(1L, "unknown", "flag"));
    }

    @Test
    void validateFlagAlreadyValidated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(new Challenge()));
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "c1")).thenReturn(true);

        assertTrue(arenaService.validateFlag(1L, "c1", "ANY"));
        verify(userRepository, never()).save(any());
    }
}