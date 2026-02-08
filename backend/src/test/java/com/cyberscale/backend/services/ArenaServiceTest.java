package com.cyberscale.backend.services;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.models.UserChallenge;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void startChallengeEnvironmentSuccess() {
        when(containerService.createChallengeContainer(anyString(), anyString())).thenReturn("container-123");
        when(challengeRepository.findById("chall-1")).thenReturn(Optional.of(new Challenge("chall-1", "Linux", "Easy", "FLAG", 100)));

        String containerId = arenaService.startChallengeEnvironment(1L, "chall-1");

        assertEquals("container-123", containerId);
    }

    @Test
    void validateFlagSuccess() {
        User user = new User();
        user.setId(1L);
        Challenge challenge = new Challenge("chall-1", "Linux", "Easy", "SUPERFLAG", 100);

        when(challengeRepository.findById("chall-1")).thenReturn(Optional.of(challenge));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // ✅ CORRECTION : On mocke la méthode qui existe réellement dans le Repository
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "chall-1")).thenReturn(false);

        boolean result = arenaService.validateFlag(1L, "chall-1", "SUPERFLAG");

        assertTrue(result);
        verify(userChallengeRepository).save(any(UserChallenge.class));
    }

    @Test
    void validateFlagFailure() {
        // ✅ CORRECTION : On mocke l'utilisateur pour éviter le "User introuvable" (404)
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Challenge challenge = new Challenge("chall-1", "Linux", "Easy", "SUPERFLAG", 100);
        when(challengeRepository.findById("chall-1")).thenReturn(Optional.of(challenge));

        boolean result = arenaService.validateFlag(1L, "chall-1", "WRONG_FLAG");

        assertFalse(result);
        verify(userChallengeRepository, never()).save(any(UserChallenge.class));
    }
}