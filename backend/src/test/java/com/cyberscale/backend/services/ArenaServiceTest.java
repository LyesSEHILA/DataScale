package com.cyberscale.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ArenaServiceTest {

    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ArenaService arenaService;

    private User mockUser;
    private Challenge mockChallenge;

    @BeforeEach
    void setUp() {
        mockUser = new User("Hacker", "hacker@test.com", "pass");
        mockUser.setId(1L);
        mockUser.setPoints(0);

        mockChallenge = new Challenge("CTF_1", "Test Challenge", "SECRET_FLAG", 100);
    }

    @Test
    void validateFlag_Success_ShouldAddPoints() {
        when(challengeRepository.findById("CTF_1")).thenReturn(Optional.of(mockChallenge));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        boolean result = arenaService.validateFlag(1L, "CTF_1", "SECRET_FLAG");

        assertTrue(result, "Le flag devrait être validé");
        assertEquals(100, mockUser.getPoints(), "L'utilisateur doit recevoir 100 points");
        verify(userRepository).save(mockUser);
    }

    @Test
    void validateFlag_Failure_ShouldNotAddPoints() {
        when(challengeRepository.findById("CTF_1")).thenReturn(Optional.of(mockChallenge));
        // Pas besoin de mocker l'user car on s'arrête avant si le flag est faux

        boolean result = arenaService.validateFlag(1L, "CTF_1", "WRONG_FLAG");

        assertFalse(result, "Le flag devrait être rejeté");
        assertEquals(0, mockUser.getPoints(), "Aucun point ne doit être ajouté");
        verify(userRepository, never()).save(any());
    }
}